package com.anjor.bookscout.di

import com.anjor.bookscout.data.remote.OpenLibraryApi
import com.anjor.bookscout.util.Constants
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Manual dependency wiring for the network layer (no Hilt/Dagger, to keep the build simple).
 *
 * - [Json] is configured to be lenient and to ignore unknown keys, so a changing or
 *   partially documented API response still parses instead of crashing.
 * - [OkHttpClient] has explicit connect/read/write timeouts so a slow or unreachable
 *   server turns into a clean [com.anjor.bookscout.util.ErrorType.TIMEOUT] rather than
 *   hanging forever.
 */
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val loggingInterceptor: HttpLoggingInterceptor
        get() = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

    private fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    fun provideOpenLibraryApi(): OpenLibraryApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(provideOkHttp())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return retrofit.create(OpenLibraryApi::class.java)
    }
}
