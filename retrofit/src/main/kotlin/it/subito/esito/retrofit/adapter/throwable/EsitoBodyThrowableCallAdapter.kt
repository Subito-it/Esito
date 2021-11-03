package it.subito.esito.retrofit.adapter.throwable

import it.subito.esito.core.Result
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class EsitoBodyThrowableCallAdapter<T>(
    private val clazz: Class<T>
) : CallAdapter<T, Call<Result<T, Throwable>>> {

    override fun responseType(): Type = clazz

    override fun adapt(call: Call<T>): Call<Result<T, Throwable>> = EsitoBodyThrowableCall(call)
}
