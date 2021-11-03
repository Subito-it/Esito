package it.subito.esito.retrofit.error

import it.subito.esito.core.Result
import it.subito.esito.retrofit.NetworkError
import it.subito.esito.retrofit.data.TestErrorResponse
import it.subito.esito.retrofit.data.TestOkResponse
import it.subito.esito.retrofit.fold
import junit.framework.TestCase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
class NetworkErrorIntegrationTest {

    private val server = MockWebServer()
    private val service: TestService = TestService(server.url("/"))

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

        service.ok().fold(
            onSuccess = {
                assertEquals(TestOkResponse("hello"), it)
            },
            onServerError = { fail() },
            onUnexpectedError = { fail() }
        )
    }

    @Test
    fun okResponse() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(203)
                .setBody(
                    """
            {
                message: "hello"
            }
        """.trimIndent()
                )
        )

        service.okResponse().fold(
            onSuccess = {
                assertEquals(TestOkResponse("hello"), it.body())
                assertEquals(203, it.code())
                assertTrue(it.isSuccessful)
            },
            onServerError = { fail() },
            onUnexpectedError = { fail() }
        )
    }

    @Test
    fun serverErrorOkParsing() = runBlocking {
        val body = """
            {
                error: "error from server"
            }
        """.trimIndent()
        server.enqueue(
            MockResponse()
                .setBody(
                    body
                ).setResponseCode(500)
        )

        val serverError: Result<TestOkResponse, NetworkError<TestErrorResponse>> = service.serverError()
        serverError.fold(
            onSuccess = { fail() },
            onServerError = { response ->
                assertEquals(500, response.code)
                assertEquals("error from server", response.parsedBodyOrNull!!.message)
            },
            onUnexpectedError = { fail() }
        )
    }

    @Test
    fun serverErrorOkParsingResponse() = runBlocking {
        val body = """
            {
                error: "error from server"
            }
        """.trimIndent()
        server.enqueue(
            MockResponse()
                .setBody(
                    body
                ).setResponseCode(503)
        )

        service.serverErrorResponse().fold(
            onSuccess = { fail() },
            onServerError = { response ->
                assertEquals(503, response.code)
                assertEquals("error from server", response.parsedBodyOrNull!!.message)
            },
            onUnexpectedError = { fail() }
        )

    }

    @Test
    fun serverErrorFailParsing() = runBlocking {
        val body = """
            {
                missingKey: "error from server"
            }
        """.trimIndent()
        server.enqueue(
            MockResponse()
                .setBody(
                    body
                ).setResponseCode(500)
        )

        service.serverError().fold(
            onSuccess = { fail() },
            onServerError = { response ->
                assertEquals(500, response.code)
                assertNull(response.parsedBodyOrNull)
            },
            onUnexpectedError = { fail() }
        )

    }

    @Test
    fun serverErrorFailParsingResponse() = runBlocking {
        val body = """
            {
                missingKey: "error from server"
            }
        """.trimIndent()
        server.enqueue(
            MockResponse()
                .setBody(
                    body
                ).setResponseCode(503)
        )

        service.serverErrorResponse().fold(
            onSuccess = { fail() },
            onServerError = { response ->
                assertEquals(503, response.code)
                assertNull(response.parsedBodyOrNull)
            },
            onUnexpectedError = { fail() }
        )

    }

    @Test
    fun parsingErrorResponse() = runBlocking {
        server.enqueue(
            MockResponse().setBody("")
        )

        service.parsingError().fold(
            onSuccess = { fail() },
            onServerError = { fail() },
            onUnexpectedError = {
                assertEquals(
                    "Expected start of the object '{', but had 'EOF' instead\n" +
                            "JSON input: ", it.cause.message
                )
            }
        )
    }

    @Test
    fun parsingErrorResponseResponse() = runBlocking {
        server.enqueue(
            MockResponse().setBody("")
        )

        service.parsingErrorResponse().fold(
            onSuccess = { fail() },
            onServerError = { fail() },
            onUnexpectedError = {
                assertEquals(
                    "Expected start of the object '{', but had 'EOF' instead\n" +
                            "JSON input: ", it.cause.message
                )
            }
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }
}
