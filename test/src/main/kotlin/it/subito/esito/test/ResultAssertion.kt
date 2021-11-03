package it.subito.esito.test

import it.subito.esito.core.Failure
import it.subito.esito.core.Result
import it.subito.esito.core.Success
import junit.framework.TestCase.assertTrue


/**
 * Assert that [this] is a [Success].
 *
 * @param contentAssertBlock a lambda to run assertions on a [Success]
 */
fun <V, E> Result<V, E>.assertIsSuccess(
    contentAssertBlock: Success<V, E>.() -> Unit = {}
) {
    assertTrue("Expected Success but it was $this", this is Success)
    contentAssertBlock(this as Success<V, E>)
}

/**
 * Assert that [this] is an [Failure].
 *
 * @param errorAssertBlock a lambda to run assertions on a [Failure]
 */
fun <V, E> Result<V, E>.assertIsFailure(
    errorAssertBlock: Failure<V, E>.() -> Unit = {}
) {
    assertTrue("Expected Failure but it was $this", this is Failure)
    errorAssertBlock(this as Failure<V, E>)
}
