package it.subito.esito.async

import it.subito.esito.core.Result
import it.subito.esito.core.flatMap
import it.subito.esito.core.map

fun <V1, V2, V3, E> Result<V1, E>.zip(second: Result<V2, E>, f: (V1, V2) -> V3): Result<V3, E> =
    flatMap { firstValue: V1 ->
        second.map { secondValue: V2 -> f(firstValue, secondValue) }
    }
