package it.subito.esito.retrofit.adapter.throwable

import it.subito.esito.core.Result
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class EsitoResponseThrowableCall<T>(
    private val rawCall: Call<T>
) : Call<Result<Response<T>, Throwable>> {

    override fun clone(): Call<Result<Response<T>, Throwable>> = EsitoResponseThrowableCall(rawCall.clone())

    override fun enqueue(callback: Callback<Result<Response<T>, Throwable>>): Unit =
        rawCall.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {

                val result: Response<Result<Response<T>, Throwable>> = if (response.isSuccessful) {
                    Response.success(Result.success<Response<T>, Throwable>(response))
                } else {
                    Response.success(Result.failure(HttpException(response)))
                }

                callback.onResponse(this@EsitoResponseThrowableCall, result)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                val result = Result.failure<Response<T>, Throwable>(t)
                callback.onResponse(this@EsitoResponseThrowableCall, Response.success(result))
            }
        })

    override fun execute(): Response<Result<Response<T>, Throwable>> {
        throw NotImplementedError("Esito Result Type must be used with suspend functions")
    }

    override fun isExecuted(): Boolean = rawCall.isExecuted

    override fun cancel(): Unit = rawCall.cancel()

    override fun isCanceled(): Boolean = rawCall.isCanceled

    override fun request(): Request = rawCall.request()

    override fun timeout(): Timeout = rawCall.timeout()

}
