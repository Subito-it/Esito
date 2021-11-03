package it.subito.esito.retrofit.adapter.throwable

import it.subito.esito.core.Result
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Response
import java.lang.reflect.Type

class EsitoResponseThrowableCallAdapter<T>(
    private val clazz: Class<T>
) : CallAdapter<T, Call<Result<Response<T>, Throwable>>> {

    override fun responseType(): Type = clazz

    override fun adapt(call: Call<T>): Call<Result<Response<T>, Throwable>> = EsitoResponseThrowableCall(call)

}
