package com.jcaa.usersmanagement.mobile.data

import com.google.gson.Gson
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    fun create(baseUrl: String, token: String?): UserApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                if (!token.isNullOrBlank()) {
                    builder.header("Authorization", "Bearer $token")
                }
                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(normalize(baseUrl))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }

    private fun normalize(baseUrl: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        return "$trimmed/"
    }
}

fun Throwable.userMessage(): String {
    if (this is HttpException) {
        val raw = response()?.errorBody()?.string().orEmpty()
        if (raw.isNotBlank()) {
            return try {
                Gson().fromJson(raw, ApiError::class.java).message
            } catch (_: Exception) {
                raw
            }
        }
        return "Error HTTP ${code()}"
    }
    if (this is IOException) {
        return "No se pudo conectar con el backend. Revisa la URL y que la API este encendida."
    }
    return message ?: "Error inesperado"
}
