package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// === ALADHAN PRAYER TIMES API MODELS ===

@JsonClass(generateAdapter = true)
data class AladhanResponse(
    val code: Int,
    val status: String,
    val data: AladhanData
)

@JsonClass(generateAdapter = true)
data class AladhanData(
    val timings: AladhanTimings,
    val date: AladhanDate
)

@JsonClass(generateAdapter = true)
data class AladhanTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Sunset") val sunset: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String,
    @Json(name = "Imsak") val imsak: String,
    @Json(name = "Midnight") val midnight: String
)

@JsonClass(generateAdapter = true)
data class AladhanDate(
    val readable: String,
    val timestamp: String,
    val hijri: HijriDate,
    val gregorian: GregorianDate
)

@JsonClass(generateAdapter = true)
data class HijriDate(
    val date: String,
    val format: String,
    val day: String,
    val weekday: HijriWeekday,
    val month: HijriMonth,
    val year: String
)

@JsonClass(generateAdapter = true)
data class HijriWeekday(
    val en: String,
    val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class HijriMonth(
    val number: Int,
    val en: String,
    val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class GregorianDate(
    val date: String,
    val format: String,
    val day: String,
    val weekday: GregorianWeekday,
    val month: GregorianMonth,
    val year: String
)

@JsonClass(generateAdapter = true)
data class GregorianWeekday(
    val en: String
)

@JsonClass(generateAdapter = true)
data class GregorianMonth(
    val number: Int,
    val en: String
)


// === ALQURAN CLOUD API MODELS ===

@JsonClass(generateAdapter = true)
data class QuranSurahListResponse(
    val code: Int,
    val status: String,
    val data: List<QuranSurahRemote>
)

@JsonClass(generateAdapter = true)
data class QuranSurahRemote(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

@JsonClass(generateAdapter = true)
data class QuranSurahEditionsResponse(
    val code: Int,
    val status: String,
    val data: List<QuranEditionRemote>
)

@JsonClass(generateAdapter = true)
data class QuranEditionRemote(
    val number: Int,
    val name: String,
    val englishName: String,
    val ayahs: List<QuranAyahRemote>
)

@JsonClass(generateAdapter = true)
data class QuranAyahRemote(
    val number: Int,
    val audio: String? = null,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int
)
