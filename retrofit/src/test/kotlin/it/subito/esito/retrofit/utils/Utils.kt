package it.subito.esito.retrofit.utils

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import it.subito.esito.retrofit.adapter.EsitoCallAdapterFactory
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

fun getRetrofit(baseUrl: HttpUrl): Retrofit {
    val contentType = "application/json".toMediaType()
    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        allowSpecialFloatingPointValues = true
        prettyPrint = true
    }

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(jsonConfig.asConverterFactory(contentType))
        .addCallAdapterFactory(EsitoCallAdapterFactory.create())
        .build()
}
