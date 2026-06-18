package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Prayer Timings
    @Query("SELECT * FROM prayer_timings WHERE date = :date AND method = :method AND (city = :city OR (latitude BETWEEN :lat-0.1 AND :lat+0.1 AND longitude BETWEEN :lng-0.1 AND :lng+0.1)) LIMIT 1")
    suspend fun getPrayerTimings(date: String, method: Int, city: String, lat: Double, lng: Double): PrayerTimingsEntity?

    @Query("SELECT * FROM prayer_timings WHERE date = :date LIMIT 1")
    suspend fun getPrayerTimingsByDate(date: String): PrayerTimingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimings(timings: PrayerTimingsEntity)

    // App Preferences
    @Query("SELECT * FROM app_preferences WHERE `key` = :key LIMIT 1")
    suspend fun getPreference(key: String): AppPreferenceEntity?

    @Query("SELECT * FROM app_preferences")
    fun getAllPreferencesFlow(): Flow<List<AppPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: AppPreferenceEntity)

    @Query("DELETE FROM app_preferences WHERE `key` = :key")
    suspend fun deletePreference(key: String)

    // Quran Surahs cache
    @Query("SELECT * FROM quran_surahs ORDER BY number ASC")
    fun getCachedSurahsFlow(): Flow<List<QuranSurahEntity>>

    @Query("SELECT * FROM quran_surahs ORDER BY number ASC")
    suspend fun getCachedSurahs(): List<QuranSurahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<QuranSurahEntity>)

    // Quran Ayahs cache
    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber AND languageCode = :langCode ORDER BY ayahNumber ASC")
    fun getCachedAyahsFlow(surahNumber: Int, langCode: String): Flow<List<QuranAyahEntity>>

    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber AND languageCode = :langCode ORDER BY ayahNumber ASC")
    suspend fun getCachedAyahs(surahNumber: Int, langCode: String): List<QuranAyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<QuranAyahEntity>)
}
