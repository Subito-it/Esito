package it.subito.esito.retrofit.throwable

import it.subito.esito.core.Result
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import it.subito.esito.retrofit.adapter.EsitoCallAdapterFactory
import it.subito.esito.retrofit.data.TestOkResponse
import it.subito.esito.retrofit.utils.getRetrofit
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.HttpUrl
import retrofit2.Response


interface TestService {

    @GET("ok")
    suspend fun ok(): Result<TestOkResponse, Throwable>

    @GET("okResponse")
    suspend fun okResponse(): Result<Response<TestOkResponse>, Throwable>

    @GET("serverError")
    suspend fun serverError(): Result<TestOkResponse, Throwable>

    @GET("serverErrorResponse")
    suspend fun serverErrorResponse(): Result<Response<TestOkResponse>, Throwable>

    @GET("parsingError")
    suspend fun parsingError(): Result<TestOkResponse, Throwable>

    @GET("parsingErrorResponse")
    suspend fun parsingErrorResponse(): Result<Response<TestOkResponse>, Throwable>

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        operator fun invoke(baseUrl: HttpUrl): TestService = getRetrofit(baseUrl).create(TestService::class.java)
    }
}
