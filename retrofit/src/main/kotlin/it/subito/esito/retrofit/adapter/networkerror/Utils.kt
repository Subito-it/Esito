package it.subito.esito.retrofit.adapter.networkerror

import it.subito.esito.retrofit.NetworkError
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response

internal fun <T, R> Response<T>.extractError(
    errorParser: Converter<ResponseBody, R>,
): NetworkError<R> = errorBody().use { errorBody ->

    val parsedError = kotlin.runCatching {
        errorBody?.let { body -> errorParser.convert(body) }
    }.getOrNull()

    return NetworkError.ServerError(
        code = code(),
        parsedBodyOrNull = parsedError,
    )
}
