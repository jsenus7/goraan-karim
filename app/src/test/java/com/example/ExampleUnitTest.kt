package com.example

import com.example.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testAlquranApiParsesCorrectly() = runBlocking {
    val response = RetrofitClient.quranService.getSurahEditions(1, "quran-uthmani,en.sahih,ar.alafasy")
    assertEquals(200, response.code)
    assertEquals(3, response.data.size)
    assertNotNull(response.data[0].ayahs)
  }
}





