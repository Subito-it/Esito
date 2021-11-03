package it.subito.esito.core

import it.subito.esito.test.assertIsFailure
import it.subito.esito.test.assertIsSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ResultTest {

    @get:Rule
    val coroutineRule = CoroutineScopeRule()

    @Test
    fun fold_success() {
        var invoked = false
        val value = "123"
        successResult(value).fold(
            onSuccess = {
                assertEquals(value, it)
                invoked = true
            },
            onFailure = { fail() }
        )
        assertTrue(invoked)
    }

    @Test
    fun fold_failure() {
        var invoked = false
        val error = "123"
        failureResult(error).fold(
            onSuccess = { fail() },
            onFailure = {
                assertEquals(error, it)
                invoked = true
            }
        )
        assertTrue(invoked)
    }

    @Test
    fun companion_success() {
        assertTrue(Result.success<String, Unit>("") is Success)
    }

    @Test
    fun companion_failure() {
        assertTrue(Result.failure<Unit, String>("") is Failure)
    }

    @Test
    fun getOrNull_success() {
        val expected = "123"
        assertEquals(expected, successResult(expected).getOrNull())
    }

    @Test
    fun getOrNull_failure() {
        assertNull(failureResult().getOrNull())
    }

    @Test
    fun errorOrNull_success() {
        assertNull(successResult().errorOrNull())
    }

    @Test
    fun errorOrNull_failure() {
        val expected = "123"
        assertEquals(expected, failureResult(expected).errorOrNull())
    }

    @Test
    fun runCatching_success() {
        val value = "123"
        val result = runCatching {
            println(this@runCatching)
            value
        }
        assertTrue(result is Success)
        assertEquals(value, result.getOrNull())
    }

    @Test
    fun runCatching_failure() {
        val error = IllegalStateException()
        val result = runCatching { throw error }
        assertTrue(result is Failure)
        assertEquals(error, result.errorOrNull())
    }

    @Test
    fun runCatchingWithReceiver_success() {
        val expected = "123"
        val result = "".runCatching { expected }
        assertTrue(result is Success)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun runCatchingWithReceiver_failure() {
        val expected = IllegalStateException()
        val result = "".runCatching { throw expected }
        assertTrue(result is Failure)
        assertEquals(expected, result.errorOrNull())
    }

    @Test
    fun getOrThrow_success() {
        val expected = "123"
        assertEquals(expected, Result.success<String, Throwable>(expected).getOrThrow())
    }

    @Test(expected = IllegalStateException::class)
    fun getOrThrow_failure() {
        Result.failure<String, Throwable>(IllegalStateException()).getOrThrow()
    }

    @Test
    fun getOrDefault_success() {
        val expected = "123"
        assertEquals(expected, successResult(expected).getOrDefault("aaa"))
    }

    @Test
    fun getOrDefault_failure() {
        val expected = "123"
        assertEquals(expected, failureResult().getOrDefault(expected))
    }

    @Test
    fun getOrElse_success() {
        val expected = "123"
        assertEquals(expected, successResult(expected).getOrElse { "aaa" })
    }

    @Test
    fun getOrElse_failure() {
        val expected = "123"
        assertEquals(expected, Result.failure<String, String>("aaa").getOrElse { expected })
    }

    @Test
    fun map_success() {
        val expected = "aaa"
        val result = successResult("123").map { expected }
        assertTrue(result is Success)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun map_failure() {
        val expected = "123"
        val result = failureResult(expected).map { "aaa" }
        assertTrue(result is Failure)
        assertEquals(expected, result.errorOrNull())
    }

    @Test(expected = IllegalStateException::class)
    fun map_throwsException() {
        successResult().map { throw IllegalStateException() }
    }

    @Test
    fun recover_success() {
        val expected = "123"
        val result = successResult(expected).recover { "aaa" }
        assertTrue(result is Success)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun recover_failure() {
        val expected = "123"
        val result = Result.failure<String, String>("aaa").recover { expected }
        assertTrue(result is Success)
        assertEquals(expected, result.getOrNull())
    }

    @Test(expected = IllegalStateException::class)
    fun recover_throwsException() {
        failureResult().recover { throw IllegalStateException() }
    }

    @Test
    fun flatRecover_success() {
        val expected = "123"
        val result = successResult(expected).flatRecover { successResult("aaa") }
        assertTrue(result is Success)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun flatRecover_failure() {
        val expected = "123"
        val result = Result.failure<String, String>("aaa").flatRecover { Result.success(expected) }
        assertTrue(result is Success)
        assertEquals(expected, result.getOrNull())
    }

    @Test(expected = IllegalStateException::class)
    fun flatRecover_throwsException() {
        failureResult().flatRecover { throw IllegalStateException() }
    }

    @Test
    fun mapCatching_success() {
        val expected = "123"
        val result = Result.success<String, Throwable>("aaa").mapCatching { expected }
        assertTrue(result is Success)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun mapCatching_failure() {
        val expected = IllegalStateException()
        val result = Result.failure<String, Throwable>(expected).mapCatching { "aaa" }
        assertTrue(result is Failure)
        assertEquals(expected, result.errorOrNull())
    }

    @Test
    fun mapCatching_throwsException() {
        val expected = IllegalStateException()
        val result = Result.success<String, Throwable>("aaa").mapCatching { throw expected }
        assertTrue(result is Failure)
        assertEquals(expected, result.errorOrNull())
    }

    @Test
    fun recoverCatching_success() {
        val expected = "123"
        val result = successResult(expected).recoverCatching { "aaa" }
        assertTrue(result is Success)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun recoverCatching_failure() {
        val expected = "123"
        val result = Result.failure<String, String>("aaa").recoverCatching { "123" }
        assertTrue(result is Success)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun recoverCatching_throwsException() {
        val expected = IllegalStateException()
        val result = Result.failure<String, String>("aaa").recoverCatching { throw expected }
        assertTrue(result is Failure)
        assertEquals(expected, result.errorOrNull())
    }

    @Test
    fun onSuccess_success() {
        val value = "123"
        var invoked = false
        successResult(value).onSuccess {
            assertEquals(value, it)
            invoked = true
        }
        assertTrue(invoked)
    }

    @Test
    fun onSuccess_failure() {
        failureResult("123").onSuccess { fail() }
    }

    @Test
    fun onFailure_success() {
        successResult("123").onFailure { fail() }
    }

    @Test
    fun onFailure_failure() {
        val error = "123"
        var invoked = false
        failureResult(error).onFailure {
            assertEquals(error, it)
            invoked = true
        }
        assertTrue(invoked)
    }

    @Test
    fun runCatchingSuspend_success() = coroutineRule.runBlockingTest {
        val result = runCatchingSuspend {
            delay(2000)
            "Success"
        }

        advanceTimeBy(2000)

        result.assertIsSuccess {
            assertEquals("Success", value)
        }
    }

    @Test
    fun runCatchingSuspend_failure() = coroutineRule.runBlockingTest {
        val exception = IllegalStateException()

        val result = runCatchingSuspend {
            delay(2000)
            throw exception
        }

        advanceTimeBy(2000)

        result.assertIsFailure {
            assertEquals(exception, error)
        }

    }

    @Test
    fun runCatchingSuspend_cancellation() = coroutineRule.runBlockingTest {
        val exception = IllegalStateException()


        val job = launch {
            runCatchingSuspend {
                delay(2000)
                throw exception
            }

            fail()
        }

        advanceTimeBy(1000)

        job.cancel()

        assertTrue(job.isCancelled)
    }

    @Test
    fun runCatchingSuspendWithReceiver_success() = coroutineRule.runBlockingTest {
        val result = "Success".runCatchingSuspend {
            delay(2000)
            this
        }

        advanceTimeBy(2000)

        result.assertIsSuccess {
            assertEquals("Success", value)
        }

    }

    @Test
    fun runCatchingSuspendWithReceiver_failure() = coroutineRule.runBlockingTest {
        val exception = IllegalStateException()

        val result = "".runCatchingSuspend {
            delay(2000)
            throw exception
        }

        advanceTimeBy(2000)

        result.assertIsFailure {
            assertEquals(exception, error)
        }

    }

    @Test
    fun runCatchingSuspendWithReceiver_cancellation() = coroutineRule.runBlockingTest {
        val exception = IllegalStateException()

        val job = launch {
            "".runCatchingSuspend {
                delay(2000)
                throw exception
            }

            fail()
        }

        advanceTimeBy(1000)

        job.cancel()

        assertTrue(job.isCancelled)
    }

    private fun successResult() = Result.success<String, Unit>("")

    private fun failureResult() = Result.failure<Unit, String>("")

    private fun <V> successResult(value: V) = Result.success<V, Unit>(value)

    private fun <E> failureResult(error: E) = Result.failure<Unit, E>(error)
}
