package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface AladhanApiService {
    @GET("v1/timings")
    suspend fun getTimings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int
    ): AladhanResponse

    @GET("v1/timingsByCity")
    suspend fun getTimingsByCity(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int
    ): AladhanResponse
}

interface QuranApiService {
    @GET("v1/surah")
    suspend fun getSurahs(): QuranSurahListResponse

    // Fetch double parallel editions, e.g., "quran-uthmani,en.sahih"
    @GET("v1/surah/{number}/editions/{editions}")
    suspend fun getSurahEditions(
        @Path("number") number: Int,
        @Path("editions") editions: String // format: "quran-uthmani,en.sahih" or "quran-uthmani,ar.alafasy" etc.
    ): QuranSurahEditionsResponse
}

object RetrofitClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val aladhanService: AladhanApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(AladhanApiService::class.java)
    }

    val quranService: QuranApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.alquran.cloud/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(QuranApiService::class.java)
    }
}
