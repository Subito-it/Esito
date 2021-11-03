package it.subito.esito.retrofit.adapter.throwable

import it.subito.esito.core.Result
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class EsitoBodyThrowableCall<T>(
    private val rawCall: Call<T>
) : Call<Result<T, Throwable>> {

    override fun clone(): Call<Result<T, Throwable>> = EsitoBodyThrowableCall(rawCall.clone())

    override fun enqueue(callback: Callback<Result<T, Throwable>>): Unit = rawCall.enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            callback.onResponse(this@EsitoBodyThrowableCall, Response.success(response.toEsitoResult()))
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            val result = Result.failure<T, Throwable>(t)
            callback.onResponse(this@EsitoBodyThrowableCall, Response.success(result))
        }
    })

    override fun execute(): Response<Result<T, Throwable>> =
        throw NotImplementedError("Esito Result Type must be used with suspend functions")

    override fun isExecuted(): Boolean = rawCall.isExecuted

    override fun cancel(): Unit = rawCall.cancel()

    override fun isCanceled(): Boolean = rawCall.isCanceled

    override fun request(): Request = rawCall.request()

    override fun timeout(): Timeout = rawCall.timeout()

    private fun <T> Response<T>.toEsitoResult(): Result<T, Throwable> {
        return if (isSuccessful) {
            Result.success(body()!!)
        } else {
            Result.failure(HttpException(this))
        }
    }

}
