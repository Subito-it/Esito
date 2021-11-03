# Esito

Esito integration for [Retrofit](https://github.com/square/retrofit/).

## Installation

```groovy
repositories {
	maven { url 'https://jitpack.io' }
}

dependencies {
    implementation("com.github.Subito-it.Esito:retrofit:(insert latest version)")
}
```

### Available Types

 - `Result<T,Throwable>` and `Result<Response<T>, Throwable>`, where `T` is the body type.
 - `Result<T, NetworkError<R>>` and  `Result<Response<T>, NetworkError<R>>` where `T` is the body type for successful responses, and `R` is the body type for unsuccessful responses.

### Usage

Add `EsitoCallAdapterFactory` as a `Call` adapter when building your `Retrofit` instance:


```kotlin
Retrofit.Builder()
        .baseUrl("https://example.com/")
        .addCallAdapterFactory(EsitoCallAdapterFactory.create())
        .build()
```

Your service methods can now use any of the above types as their return type.

#### Usage with Throwable

Your service methods can now return `Result<T,Throwable>` and `Result<Response<T>, Throwable>`:

```kotlin
interface TestService {

    @GET("/some/path")
    suspend fun getData(): Result<TestOkResponse, Throwable>

    @GET("/some/path")
    suspend fun getDataInResponse(): Result<Response<TestOkResponse>, Throwable>
    
}

@Serializable
data class TestOkResponse(
    @SerialName("message") val message: String
)


```

The result will be a `Success` for [successful](https://square.github.io/retrofit/2.x/retrofit/retrofit2/Response.html#isSuccessful--) responses, and it will be a `Failure` fo unsuccessful ones or if an unexpected event occurs.

The Throwable will be an instance of [HttpException](https://square.github.io/retrofit/2.x/retrofit/retrofit2/HttpException.html) for all non-2xx HTTP responses, and it can be used in this way:

```kotlin
val data = service.getData().fold(
            onSuccess = {
                // Use the successful body
            },
            onFailure = {
                if (it is HttpException) {
                    // Access to specific fields such as status code
                } else {
                    // Unexpected event
                }
            })

```

#### Usage with NetworkError

If you like a more typed way of handling errors, Esito is providing a class for network failures:

```kotlin
sealed class NetworkError<out T> {

    data class ServerError<T>(
        val code: Int,
        val parsedBodyOrNull: T?,
    ) : NetworkError<T>()

    data class UnexpectedError(
        val cause: Throwable
    ) : NetworkError<Nothing>()
}
```

Your service methods can return `Result<T, NetworkError<R>>` and  `Result<Response<T>, NetworkError<R>>`, for example:

```kotlin
interface TestService {

    @GET("/some/path")
    suspend fun getData(): Result<TestOkResponse, NetworkError<TestErrorResponse>>

    @GET("/some/path")
    suspend fun getDataInResponse(): Result<Response<TestOkResponse>, NetworkError<TestErrorResponse>>

}

@Serializable
data class TestOkResponse(
    @SerialName("message") val message: String
)

@Serializable
data class TestErrorResponse(
    @SerialName("error") val message: String
)

```

The result will be a `Success` for [successful](https://square.github.io/retrofit/2.x/retrofit/retrofit2/Response.html#isSuccessful--) responses, and it will be a `Failure` fo unsuccessful ones or if an unexpected event occurs.

The NetworkError will be an instance of `ServerError` for all non-2xx HTTP responses, and it will contain the parsed error body if it's possible to parse it.

All the unexpected errors will instead be returned as instances of `UnexpectedError`, containing the cause Throwable.

An additional `fold` method is provided allowing us to handle all possible errors in this way:

```kotlin
val data = service.getData().fold(
            onSuccess = {
                // Use the successful body
            },
            onServerError = {
                // Access to parsed error body and status code
            },
            onUnexpectedError = {
                // Unexpected event
            }
        )
```

