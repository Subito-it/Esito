# Esito

[![](https://jitpack.io/v/Subito-it/Esito.svg)](https://jitpack.io/#Subito-it/Esito)

Esito ambition is to be your return type for suspending functions.

## Installation

```groovy
repositories {
	maven { url 'https://jitpack.io' }
}

dependencies {
    implementation("com.github.Subito-it.Esito:core:(insert latest version)")
}
```

### Getting started

The main class is the sealed class [`Result`](core/src/main/kotlin/it/subito/esito/core/Result.kt), with two subtypes:

 - `Success`: it contains the result of a successful computation
 - `Failure`: it contains the cause of an unsuccessful computation

The goal when using Esito is to avoid throwing exceptions and use Result as the return type of a function that can fail:


```kotlin
sealed class ConversionError
object EmptyInput : ConversionError()
object NotNumericInput : ConversionError()

fun fromStringToInt(input: String): Result<Int, ConversionError> = when {
    input.isBlank() -> Result.failure(EmptyInput)
    else -> runCatching { input.toInt() }.mapError { NotNumericInput }
}
```

In this example we are defining all the possible failures in `ConversionError`, then we are applying some logic to build our result.

If the input is not blank we are using the `runCatching` method to wrap a method throwing an exception and mapping the eventual error in our desired type.

### Operators

Esito result has several operators, such as `map` and `flatmap`, for examples see [Recipes](Recipes.md).

## Retrofit Integration

Esito ships an integration with retrofit, after a one-line setup you can start to use Result return type in your Retrofit interfaces:

```kotlin
interface GitHubService {
    
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String?): Result<List<Repo>, Throwable>
}
```

For additional info and to learn how to use your own Error instead of Throwable have a look at [this documentation](/retrofit/README.md).

## Async Utilities

Esito offers some utilities for suspending methods returning Result. For example, suppose we have the following functions:

```kotlin
suspend fun getUserFullName(userId: Int): Result<String, FeatureError>

suspend fun getUserStatus(userId: Int): Result<UserStatus, FeatureError>

```

And we have to return an instance of DataForUI running in parallel `getUserFullName` and `getUserStatus`.

```kotlin
data class DataForUI(
	val userFullName: String,
	val userStatus: UserStatus
)
```

Esito exposes a `zip` method to compute two independent execution in parallel:

```kotlin
suspend fun fetchIfo(userId: Int): Result<DataForUI, FeatureError> =
	zip(
		{ getUserFullName(userId) },
		{ getUserStatus(userId) },
		::DataForUI //syntactic sugar for constructor
		).invoke()
```
For additional info have a look at this [documentation](async/README.md).

## Testing

Esito is providing two extension methods to facilitate testing: `assertIsSuccess` and `assertIsFailure`, here is an example of usage:

```kotlin
val success = Result.success<Int, Throwable>(42)
success.assertIsSuccess {
    assertEquals(42, value)
}

val failure = Result.failure<Int, RuntimeException>(RuntimeException("Ops"))
failure.assertIsFailure {
    assertEquals("Ops", error.message)
}
```
For additional info have a look at this [documentation](test/README.md).

