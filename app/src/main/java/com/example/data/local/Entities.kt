package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_timings")
data class PrayerTimingsEntity(
    @PrimaryKey val date: String, // format "dd-MM-yyyy"
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val method: Int,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val midnight: String,
    val hijriDay: String,
    val hijriMonthAr: String,
    val hijriMonthEn: String,
    val hijriYear: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_preferences")
data class AppPreferenceEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "quran_surahs")
data class QuranSurahEntity(
    @PrimaryKey val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

@Entity(tableName = "quran_ayahs")
data class QuranAyahEntity(
    @PrimaryKey val compositeKey: String, // "surahNumber_ayahNumber_langCode"
    val surahNumber: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val textTranslation: String,
    val audioUrl: String,
    val languageCode: String
)
