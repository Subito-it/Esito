package it.subito.esito.retrofit.adapter.networkerror

import it.subito.esito.core.Result
import it.subito.esito.retrofit.NetworkError
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.*
import java.lang.reflect.Type

class EsitoBodyNetworkErrorCall<T, R>(
    private val rawCall: Call<T>,
    private val retrofit: Retrofit,
    private val errorType: Type
) : Call<Result<T, NetworkError<R>>> {

    override fun clone(): Call<Result<T, NetworkError<R>>> = EsitoBodyNetworkErrorCall(
        rawCall.clone(),
        retrofit,
        errorType
    )

    override fun enqueue(callback: Callback<Result<T, NetworkError<R>>>): Unit = rawCall.enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            val errorParser: Converter<ResponseBody, R> =
                retrofit.responseBodyConverter(errorType, arrayOfNulls(0))
            val result = response.toEsitoResult(errorParser)
            callback.onResponse(this@EsitoBodyNetworkErrorCall, Response.success(result))
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            val result = Result.failure<T, NetworkError<R>>(NetworkError.UnexpectedError(t))
            callback.onResponse(this@EsitoBodyNetworkErrorCall, Response.success(result))
        }
    })

    override fun execute(): Response<Result<T, NetworkError<R>>> =
        throw NotImplementedError("Esito Result Type must be used with suspend functions")

    override fun isExecuted(): Boolean = rawCall.isExecuted

    override fun cancel(): Unit = rawCall.cancel()

    override fun isCanceled(): Boolean = rawCall.isCanceled

    override fun request(): Request = rawCall.request()

    override fun timeout(): Timeout = rawCall.timeout()

    private fun <T, R> Response<T>.toEsitoResult(
        errorParser: Converter<ResponseBody, R>,
    ): Result<T, NetworkError<R>> {
        return if (isSuccessful) {
            Result.success(body()!!)
        } else {
            Result.failure(extractError(errorParser))
        }
    }
}
