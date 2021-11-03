package it.subito.esito.async

import it.subito.esito.core.Result
import it.subito.esito.test.assertIsFailure
import it.subito.esito.test.assertIsSuccess
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SuspendExtensionsKtTest {

    @Test
    fun zip2Success() = runBlocking {

        suspend fun first(): Result<Int, String> = Result.success(1)

        suspend fun second(): Result<Int, String> = Result.success(42)

        val result = zip(::first, ::second) { a, b -> a + b }

        result().assertIsSuccess {
            assertEquals(43, value)
        }
    }

    @Test
    fun zip2FirstFailure() = runBlocking {

        suspend fun first(): Result<Int, String> = Result.failure("error")

        suspend fun second(): Result<Int, String> = Result.success(42)

        val result = zip(::first, ::second) { a, b -> a + b }

        result().assertIsFailure {
            assertEquals("error", error)
        }
    }

    @Test
    fun zip2SecondFailure() = runBlocking {

        suspend fun first(): Result<Int, String> = Result.success(1)

        suspend fun second(): Result<Int, String> = Result.failure("error")

        val result = zip(::first, ::second) { a, b -> a + b }

        result().assertIsFailure {
            assertEquals("error", error)
        }
    }

    @Test
    fun zip2BothFailure() = runBlocking {

        suspend fun first(): Result<Int, String> = Result.failure("error")

        suspend fun second(): Result<Int, String> = Result.failure("error2")

        val result = zip(::first, ::second) { a, b -> a + b }

        result().assertIsFailure {
            assertEquals("error", error)
        }
    }
}
