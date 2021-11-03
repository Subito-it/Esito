package it.subito.esito.retrofit

import it.subito.esito.core.Result

sealed class NetworkError<out T> {

    data class ServerError<T>(
        val code: Int,
        val parsedBodyOrNull: T?,
    ) : NetworkError<T>()

    data class UnexpectedError(
        val cause: Throwable
    ) : NetworkError<Nothing>()
}

fun <T, R> NetworkError<T>.fold(
    onServerError: (NetworkError.ServerError<T>) -> R,
    onUnexpectedError: (NetworkError.UnexpectedError) -> R
): R = when (this) {
    is NetworkError.ServerError -> onServerError(this)
    is NetworkError.UnexpectedError -> onUnexpectedError(this)
}

fun <V, E, R> Result<V, NetworkError<E>>.fold(
    onSuccess: (V) -> R,
    onServerError: (NetworkError.ServerError<E>) -> R,
    onUnexpectedError: (NetworkError.UnexpectedError) -> R
): R = fold(
    onSuccess = onSuccess,
    onFailure = {
        it.fold(
            onServerError = onServerError,
            onUnexpectedError = onUnexpectedError
        )
    }
)
