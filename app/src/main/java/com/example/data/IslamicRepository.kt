package com.example.data

import com.example.data.local.*
import com.example.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.atan2

class IslamicRepository(private val appDao: AppDao) {

    // === PRAYER TIMINGS ===

    suspend fun getPrayerTimings(
        date: String,
        method: Int,
        city: String,
        latitude: Double,
        longitude: Double
    ): Result<PrayerTimingsEntity> {
        return try {
            // 1. Try local cache
            val cached = appDao.getPrayerTimings(date, method, city, latitude, longitude)
            if (cached != null) {
                return Result.success(cached)
            }

            // 2. Local cache miss, fetch remote
            val response = if (latitude != 0.0 || longitude != 0.0) {
                RetrofitClient.aladhanService.getTimings(latitude, longitude, method)
            } else {
                RetrofitClient.aladhanService.getTimingsByCity(city, "United States", method)
            }

            if (response.code == 200) {
                val timings = response.data.timings
                val hijri = response.data.date.hijri
                val entity = PrayerTimingsEntity(
                    date = date,
                    city = if (city.isEmpty()) "Set Location" else city,
                    latitude = latitude,
                    longitude = longitude,
                    method = method,
                    fajr = timings.fajr,
                    sunrise = timings.sunrise,
                    dhuhr = timings.dhuhr,
                    asr = timings.asr,
                    maghrib = timings.maghrib,
                    isha = timings.isha,
                    midnight = timings.midnight,
                    hijriDay = hijri.day,
                    hijriMonthAr = hijri.month.ar ?: "",
                    hijriMonthEn = hijri.month.en,
                    hijriYear = hijri.year
                )
                // Cache it
                appDao.insertPrayerTimings(entity)
                Result.success(entity)
            } else {
                Result.failure(Exception("Aladhan API error: ${response.status}"))
            }
        } catch (e: Exception) {
            // Try last resort: any cached timings for today regardless of location, to allow offline working
            val lastResort = appDao.getPrayerTimingsByDate(date)
            if (lastResort != null) {
                Result.success(lastResort)
            } else {
                Result.failure(e)
            }
        }
    }


    // === APP PREFERENCES ===

    suspend fun savePreference(key: String, value: String) {
        appDao.insertPreference(AppPreferenceEntity(key, value))
    }

    suspend fun getPreference(key: String, defaultValue: String): String {
        return appDao.getPreference(key)?.value ?: defaultValue
    }

    fun getAllPreferencesFlow(): Flow<List<AppPreferenceEntity>> {
        return appDao.getAllPreferencesFlow()
    }


    // === QURAN SURAHS ===

    fun getCachedSurahsFlow(): Flow<List<QuranSurahEntity>> {
        return appDao.getCachedSurahsFlow()
    }

    suspend fun refreshSurahs(): Result<List<QuranSurahEntity>> {
        return try {
            val response = RetrofitClient.quranService.getSurahs()
            if (response.code == 200) {
                val entities = response.data.map {
                    QuranSurahEntity(
                        number = it.number,
                        name = it.name,
                        englishName = it.englishName,
                        englishNameTranslation = it.englishNameTranslation,
                        numberOfAyahs = it.numberOfAyahs,
                        revelationType = it.revelationType
                    )
                }
                appDao.insertSurahs(entities)
                Result.success(entities)
            } else {
                Result.failure(Exception("Alquran API error: ${response.status}"))
            }
        } catch (e: Exception) {
            val cached = appDao.getCachedSurahs()
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }


    // === QURAN AYAHS ===

    fun getCachedAyahsFlow(surahNumber: Int, langCode: String): Flow<List<QuranAyahEntity>> {
        return appDao.getCachedAyahsFlow(surahNumber, langCode)
    }

    suspend fun getOrFetchAyahs(
        surahNumber: Int,
        translationEdition: String,
        reciterEdition: String
    ): Result<List<QuranAyahEntity>> {
        val langCode = translationEdition.split(".").firstOrNull() ?: "en"
        try {
            // 1. Try local cache
            val cached = appDao.getCachedAyahs(surahNumber, langCode)
            if (cached.isNotEmpty()) {
                return Result.success(cached)
            }

            // 2. Fetch remote parallel editions: Arabic text, Translation text, Qari audio
            val editionsString = "quran-uthmani,$translationEdition,$reciterEdition"
            val response = RetrofitClient.quranService.getSurahEditions(surahNumber, editionsString)

            if (response.code == 200 && response.data.size >= 3) {
                val arabicData = response.data[0]
                val translationData = response.data[1]
                val audioData = response.data[2]

                val ayahsCount = arabicData.ayahs.size
                val mappedAyahs = ArrayList<QuranAyahEntity>()

                for (i in 0 until ayahsCount) {
                    val arabicAyah = arabicData.ayahs[i]
                    val translationAyah = translationData.ayahs[i]
                    val audioAyah = audioData.ayahs[i]

                    mappedAyahs.add(
                        QuranAyahEntity(
                            compositeKey = "${surahNumber}_${arabicAyah.numberInSurah}_$langCode",
                            surahNumber = surahNumber,
                            ayahNumber = arabicAyah.numberInSurah,
                            textArabic = arabicAyah.text,
                            textTranslation = translationAyah.text,
                            audioUrl = audioAyah.audio ?: "",
                            languageCode = langCode
                        )
                    )
                }

                // Cache in database
                appDao.insertAyahs(mappedAyahs)
                return Result.success(mappedAyahs)
            } else {
                return Result.failure(Exception("Could not fetch standard triple editions"))
            }
        } catch (e: Exception) {
            val cached = appDao.getCachedAyahs(surahNumber, langCode)
            return if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }


    // === QIBLA CALCULATION ===

    /**
     * Calculates the Qibla bearing (direction to the Kaaba) in degrees from current coordinates.
     * Kaaba: lat 21.4225241, lng 39.826206
     */
    fun calculateQiblaDirection(lat: Double, lng: Double): Double {
        val kaabaLat = Math.toRadians(21.4225241)
        val kaabaLng = Math.toRadians(39.826206)
        val curLat = Math.toRadians(lat)
        val curLng = Math.toRadians(lng)

        val deltaLng = kaabaLng - curLng

        val y = sin(deltaLng)
        val x = cos(curLat) * sin(kaabaLat) - sin(curLat) * cos(kaabaLat) * cos(deltaLng)

        var qiblaDegrees = Math.toDegrees(atan2(y, x))
        qiblaDegrees = (qiblaDegrees + 360) % 360
        return qiblaDegrees
    }
}
