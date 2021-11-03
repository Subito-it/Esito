package it.subito.esito.async

import it.subito.esito.core.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

fun <V1, V2, V3, E> zip(
    f1: suspend () -> Result<V1, E>,
    f2: suspend () -> Result<V2, E>,
    f: (V1, V2) -> V3
): suspend () -> Result<V3, E> {
    return { suspendZip(f1, f2, f) }
}

suspend fun <V1, V2, V3, E> suspendZip(
    f1: suspend () -> Result<V1, E>,
    f2: suspend () -> Result<V2, E>,
    f: (V1, V2) -> V3
): Result<V3, E> = coroutineScope {

    val firstDeferred = async { f1() }
    val secondDeferred = async { f2() }

    firstDeferred.await().zip(secondDeferred.await(), f)
}
