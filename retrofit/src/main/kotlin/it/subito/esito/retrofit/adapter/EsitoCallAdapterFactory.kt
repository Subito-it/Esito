package it.subito.esito.retrofit.adapter

import it.subito.esito.core.Result
import it.subito.esito.retrofit.NetworkError
import it.subito.esito.retrofit.adapter.networkerror.EsitoBodyErrorCallAdapter
import it.subito.esito.retrofit.adapter.networkerror.EsitoResponseErrorCallAdapter
import it.subito.esito.retrofit.adapter.throwable.EsitoBodyThrowableCallAdapter
import it.subito.esito.retrofit.adapter.throwable.EsitoResponseThrowableCallAdapter
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class EsitoCallAdapterFactory private constructor() : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<out Any, out Any>? = if (Call::class.java != getRawType(returnType)) null
    else {
        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (Result::class.java == getRawType(callType)) {
            buildResultCallAdapter(callType, retrofit)
        } else null

    }

    private fun buildResultCallAdapter(callType: Type, retrofit: Retrofit): CallAdapter<out Any, out Any>? =
        when (getParameterUpperBound(1, callType as ParameterizedType)) {
            Throwable::class.java -> adapterWithThrowable(callType)
            else -> adapterWithCustomError(callType, retrofit)
        }

    private fun adapterWithCustomError(callType: Type, retrofit: Retrofit): CallAdapter<out Any, out Any>? {
        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        val errorType = getParameterUpperBound(1, callType)

        if (NetworkError::class.java != getRawType(errorType)) {
            return null // NetworkError is the only custom error allowed. We should enforce this with a lint rule.
        }

        val innerErrorType = getParameterUpperBound(0, errorType as ParameterizedType)

        return if (Response::class.java == getRawType(resultType)) {
            val successType = getParameterUpperBound(0, resultType as ParameterizedType)
            val esitoResponseErrorCallAdapter: EsitoResponseErrorCallAdapter<out Any, out Any> =
                EsitoResponseErrorCallAdapter(getRawType(successType), retrofit, innerErrorType)
            esitoResponseErrorCallAdapter //inline val -> we'll get "Not enough information to infer type variable R"
        } else {
            val esitoBodyErrorCallAdapter: EsitoBodyErrorCallAdapter<out Any, Any> =
                EsitoBodyErrorCallAdapter(getRawType(resultType), retrofit, innerErrorType)
            esitoBodyErrorCallAdapter //inline val -> we'll get "Not enough information to infer type variable R"
        }
    }

    private fun adapterWithThrowable(callType: Type): CallAdapter<out Any, out Any> {
        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        return if (Response::class.java == getRawType(resultType)) {
            val successType = getParameterUpperBound(0, resultType as ParameterizedType)
            EsitoResponseThrowableCallAdapter(getRawType(successType))
        } else {
            EsitoBodyThrowableCallAdapter(getRawType(resultType))
        }
    }

    companion object {
        fun create() = EsitoCallAdapterFactory()
    }
}
