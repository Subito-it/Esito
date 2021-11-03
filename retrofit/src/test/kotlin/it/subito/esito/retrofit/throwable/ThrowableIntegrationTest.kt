package it.subito.esito.retrofit.throwable

import it.subito.esito.core.Result
import it.subito.esito.retrofit.data.TestOkResponse
import it.subito.esito.test.assertIsFailure
import it.subito.esito.test.assertIsSuccess
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class ThrowableIntegrationTest {

    private val server = MockWebServer()
    private val service = TestService(server.url("/"))

    @Test
    fun ok() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """
            {
                message: "hello"
            }
        """.trimIndent()
            )
        )

        val ok = service.ok()
        ok.assertIsSuccess {
            assertEquals(TestOkResponse("hello"), value)
        }
    }

    @Test
    fun okResponse() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(202)
                .setBody(
                    """
            {
                message: "hello"
            }
        """.trimIndent()
                )
        )

        val ok: Result<Response<TestOkResponse>, Throwable> = service.okResponse()
        ok.assertIsSuccess {
            assertEquals(TestOkResponse("hello"), value.body())
            assertEquals(202, value.code())
            assertTrue(value.isSuccessful)
        }
    }

    @Test
    fun serverError() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
        )

        val serverError = service.serverError()
        serverError.assertIsFailure {
            val httpException = error as HttpException
            assertEquals(500, httpException.code())
            assertEquals("Server Error", httpException.message())
        }
    }

    @Test
    fun serverErrorResponse() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(503)
        )

        val serverError = service.serverErrorResponse()
        serverError.assertIsFailure {
            val httpException = error as HttpException
            assertEquals(503, httpException.code())
            assertEquals("Server Error", httpException.message())
        }
    }

    @Test
    fun parsingError() = runBlocking {
        server.enqueue(
            MockResponse().setBody("")
        )

        val parsingError = service.parsingError()
        parsingError.assertIsFailure {
            assertEquals(
                "Expected start of the object '{', but had 'EOF' instead\n" +
                        "JSON input: ", error.message
            )
        }

    }

    @Test
    fun parsingErrorResponse() = runBlocking {
        server.enqueue(
            MockResponse().setBody("")
        )

        val parsingError = service.parsingErrorResponse()
        parsingError.assertIsFailure {
            assertEquals(
                "Expected start of the object '{', but had 'EOF' instead\n" +
                        "JSON input: ", error.message
            )
        }

    }

    @After
    fun tearDown() {
        server.shutdown()
    }
}
