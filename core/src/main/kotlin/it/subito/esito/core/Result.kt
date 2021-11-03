package it.subito.esito.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.cancellation.CancellationException

/**
 * Wraps the result of some kind of action. The result can be a success or a failure.
 * In case the result is successful, the result wraps an object of type [V].
 * When there is a failure, the result wraps an object of type [E].
 * @param V the type of the successful result.
 * @param E the type of the failure.
 */
sealed class Result<out V, out E> {

    companion object {
        /**
         * Creates a new success result.
         */
        @JvmStatic
        fun <V, E> success(value: V): Result<V, E> =
            Success(value)

        /**
         * Creates a new failure result.
         */
        @JvmStatic
        fun <V, E> failure(error: E): Result<V, E> =
            Failure(error)
    }

    /**
     * A way to execute actions in both success and failure cases.
     * @param onSuccess the action that will be executed if the result is success.
     * @param onFailure the action that will be executed if the result is failure.
     * @return a new object of type [R].
     */
    inline fun <R> fold(onSuccess: (value: V) -> R, onFailure: (error: E) -> R): R =
        when (this) {
            is Success -> onSuccess(value)
            is Failure -> onFailure(error)
        }
}

/**
 * The success state of [Result].
 */
data class Success<out V, out E> internal constructor(val value: V) : Result<V, E>()

/**
 * The failure state of [Result].
 */
data class Failure<out V, out E> internal constructor(val error: E) : Result<V, E>()

/**
 * Returns the success value or null if the result is failure.
 */
fun <V> Result<V, *>.getOrNull(): V? = if (this is Success) value else null

/**
 * Return the failure object or null if the result is success.
 */
fun <E> Result<*, E>.errorOrNull(): E? = if (this is Failure) error else null

/**
 * Executes [block] and catches any [Throwable] wrapping the result or error in a [Result] object.
 * @param block the action that will be executed.
 */
inline fun <R> runCatching(block: () -> R): Result<R, Throwable> =
    try {
        Result.success(block())
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }

/**
 * [runCatching] version to be used within suspend functions that will skip catching
 * [CancellationException].
 */
suspend inline fun <R> runCatchingSuspend(block: () -> R): Result<R, Throwable> =
    try {
        Result.success(block())
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }

/**
 * Executes [block] and catches any [Throwable] wrapping the result or error in a [Result] object.
 * @param block the action that will be executed.
 */
inline fun <T, R> T.runCatching(block: T.() -> R): Result<R, Throwable> =
    try {
        Result.success(block())
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }

/**
 * [runCatching] version to be used within suspend functions that will skip catching
 * [CancellationException].
 */
suspend inline fun <T, R> T.runCatchingSuspend(block: T.() -> R): Result<R, Throwable> =
    try {
        Result.success(block())
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }

/**
 * Returns the success value or throws the error. The result receiver error type must extend [Throwable].
 */
fun <V, E : Throwable> Result<V, E>.getOrThrow(): V =
    when (this) {
        is Success -> value
        is Failure -> throw error
    }

/**
 * Returns the success value, or [defaultValue] if the result is failure.
 * @param defaultValue the default value to return in case the Result is failure.
 */
fun <R, V : R> Result<V, *>.getOrDefault(defaultValue: R): R =
    when (this) {
        is Success -> value
        is Failure -> defaultValue
    }

/**
 * Returns the success value if the result is success, or the object returned by [onFailure] function.
 * @param onFailure the function to apply in case the result is failure.
 */
inline fun <R, V : R, E> Result<V, E>.getOrElse(onFailure: (error: E) -> R): R =
    when (this) {
        is Success -> value
        is Failure -> onFailure(error)
    }

/**
 * Returns a new [Result] object mapping the success value using the [transform] function. Note that if an exception is
 * thrown in the [transform] function, it won't be wrapped in a failure [Result] object. To catch the error see [mapCatching].
 * @param transform the mapping function applied to the success value.
 */
inline fun <R, V, E> Result<V, E>.map(transform: (value: V) -> R): Result<R, E> =
    when (this) {
        is Success -> Result.success(transform(value))
        is Failure -> Result.failure(error)
    }

/**
 * Returns a new [Result] object mapping the success value using the [transform] function that returns itself a [Result] and flat maps it.
 * Note that if an exception is thrown in the [transform] function, it won't be wrapped in a failure [Result] object.
 * @param transform the mapping function applied to the success value.
 */
inline fun <R, V, E> Result<V, E>.flatMap(transform: (value: V) -> Result<R, E>): Result<R, E> =
    when (this) {
        is Success -> transform(value)
        is Failure -> Result.failure(error)
    }

/**
 * Returns a new [Result] object mapping the error using the [transform] function. Note that if an exception is
 * thrown in the [transform] function, it won't be wrapped in a failure [Result] object.
 * @param transform the mapping function applied to the error.
 */
inline fun <V, E1, E2> Result<V, E1>.mapError(transform: (value: E1) -> E2): Result<V, E2> =
    when (this) {
        is Success -> Result.success(value)
        is Failure -> Result.failure(transform(error))
    }

/**
 * Returns a new [Result] object mapping the error with the [transform] function. Note that if an exception is thrown in
 * the [transform] function, it won't be wrapped in a failure [Result] object. To catch the error see [recoverCatching].
 * @param transform the mapping function that will be applied to the error.
 */
inline fun <R, V : R, E> Result<V, E>.recover(transform: (error: E) -> R): Result<R, E> =
    when (this) {
        is Success -> Result.success(value)
        is Failure -> Result.success(transform(error))
    }

/**
 * Returns a new [Result] object mapping the error with the [transform] function that returns itself a [Result] and flat maps it.
 * Note that if an exception is thrown in the [transform] function, it won't be wrapped in a failure [Result] object.
 * @param transform the mapping function that will be applied to the error.
 */
inline fun <R, V : R, E> Result<V, E>.flatRecover(transform: (error: E) -> Result<V, E>): Result<V, E> =
    when (this) {
        is Success -> Result.success(value)
        is Failure -> transform(error)
    }

/**
 * Returns a new [Result] object applying the [transform] function to the success value. If an exception will be thrown
 * in the [transform] function, it will be wrapped in a failure [Result] object.
 * @param transform the function applied to the success value.
 */
inline fun <R, V, E : Throwable> Result<V, E>.mapCatching(transform: (value: V) -> R): Result<R, Throwable> =
    when (this) {
        is Success -> try {
            Result.success(transform(value))
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
        is Failure -> Result.failure(error)
    }

/**
 * Returns a new [Result] object applying the [transform] function to the failure value. If an exception is thrown in
 * the [transform] function, it will be wrapped in a failure [Result] object.
 * @param transform the function applied to the failure value.
 */
inline fun <R, V : R, E> Result<V, E>.recoverCatching(transform: (error: E) -> R): Result<R, Throwable> =
    when (this) {
        is Success -> Result.success(value)
        is Failure -> try {
            Result.success(transform(error))
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }

/**
 * Executes the [action] if the [Result] is success.
 * @param action the function called in case the [Result] is success.
 */
inline fun <V, E> Result<V, E>.onSuccess(action: (value: V) -> Unit): Result<V, E> = apply {
    if (this is Success) action(value)
}

/**
 * Executes the [action] if the [Result] is failure.
 * @param action the function called in case the [Result] is failure.
 */
inline fun <V, E> Result<V, E>.onFailure(action: (error: E) -> Unit): Result<V, E> = apply {
    if (this is Failure) action(error)
}

/**
 * Executes the [action] if the Flow emits a [Success].
 * @param action the function called in case the [Result] is success.
 */
inline fun <V, E> Flow<Result<V, E>>.onResultSuccess(crossinline action: (V) -> Unit): Flow<Result<V, E>> =
    onEach { if (it is Success) action(it.value) }

/**
 * Executes the [action] if the Flow emits a [Failure].
 * @param action the function called in case the [Result] is failure.
 */
inline fun <V, E> Flow<Result<V, E>>.onResultFailure(crossinline action: (E) -> Unit): Flow<Result<V, E>> =
    onEach { if (it is Failure) action(it.error) }

/**
 * Returns a new [Flow] of [Result] mapping the success value using the [transform] function that returns itself a [Flow] of [Result] and flat maps it.
 * Note that if an exception is thrown in the [transform] function, it won't be wrapped in a failure [Result] object.
 * @param transform the mapping function applied to the success value.
 */
inline fun <R, V, E> Flow<Result<V, E>>.mapResultSuccess(crossinline transform: (V) -> R): Flow<Result<R, E>> =
    map {
        when (it) {
            is Success -> Result.success(transform(it.value))
            is Failure -> Result.failure(it.error)
        }
    }
