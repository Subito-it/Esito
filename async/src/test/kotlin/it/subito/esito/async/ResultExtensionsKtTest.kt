package it.subito.esito.async

import it.subito.esito.core.Result
import it.subito.esito.test.assertIsFailure
import it.subito.esito.test.assertIsSuccess
import org.junit.Assert.assertEquals
import org.junit.Test

class ResultExtensionsKtTest {

    @Test
    fun zip2Success() {
        val first = Result.success<Int, String>(1)
        val second = Result.success<Int, String>(42)

        val result = first.zip(second) { a, b -> a + b }

        result.assertIsSuccess {
            assertEquals(43, value)
        }
    }

    @Test
    fun zip2FirstFailure() {
        val first = Result.failure<Int, String>("error")
        val second = Result.success<Int, String>(42)

        val result = first.zip(second) { a, b -> a + b }

        result.assertIsFailure {
            assertEquals("error", error)
        }
    }

    @Test
    fun zip2SecondFailure() {
        val first = Result.success<Int, String>(1)
        val second = Result.failure<Int, String>("error")

        val result = first.zip(second) { a, b -> a + b }

        result.assertIsFailure {
            assertEquals("error", error)
        }
    }

    @Test
    fun zip2BothFailure() {
        val first = Result.failure<Int, String>("error")
        val second = Result.failure<Int, String>("error2")

        val result = first.zip(second) { a, b -> a + b }

        result.assertIsFailure {
            assertEquals("error", error)
        }
    }
}
