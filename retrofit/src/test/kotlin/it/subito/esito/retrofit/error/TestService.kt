package it.subito.esito.retrofit.error

import it.subito.esito.core.Result
import it.subito.esito.retrofit.NetworkError
import it.subito.esito.retrofit.data.TestErrorResponse
import it.subito.esito.retrofit.data.TestOkResponse
import it.subito.esito.retrofit.utils.getRetrofit
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.HttpUrl
import retrofit2.Response
import retrofit2.http.GET


interface TestService {

    @GET("ok")
    suspend fun ok(): Result<TestOkResponse, NetworkError<TestErrorResponse>>

    @GET("okResponse")
    suspend fun okResponse(): Result<Response<TestOkResponse>, NetworkError<TestErrorResponse>>

    @GET("serverError")
    suspend fun serverError(): Result<TestOkResponse, NetworkError<TestErrorResponse>>

    @GET("serverErrorResponse")
    suspend fun serverErrorResponse(): Result<Response<TestOkResponse>, NetworkError<TestErrorResponse>>

    @GET("parsingError")
    suspend fun parsingError(): Result<TestOkResponse, NetworkError<TestErrorResponse>>

    @GET("parsingErrorResponse")
    suspend fun parsingErrorResponse(): Result<Response<TestOkResponse>, NetworkError<TestErrorResponse>>


    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        operator fun invoke(baseUrl: HttpUrl): TestService = getRetrofit(baseUrl).create(TestService::class.java)
    }
}
