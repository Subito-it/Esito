package it.subito.esito.retrofit.adapter.networkerror

import it.subito.esito.core.Result
import it.subito.esito.retrofit.NetworkError
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.Type

class EsitoResponseErrorCallAdapter<T, R>(
    private val clazz: Class<T>,
    private val retrofit: Retrofit,
    private val errorType: Type
) : CallAdapter<T, Call<Result<Response<T>, NetworkError<R>>>> {

    override fun responseType(): Type = clazz

    override fun adapt(call: Call<T>): Call<Result<Response<T>, NetworkError<R>>> =
        EsitoResponseNetworkErrorCall(call, retrofit, errorType)
}
