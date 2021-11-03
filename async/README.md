# Esito

Esito async utilities.

## Installation

```groovy
repositories {
	maven { url 'https://jitpack.io' }
}

dependencies {
    implementation("com.github.Subito-it.Esito:async:(insert latest version)")
}
```

### Usage

#### Suspending Methods

Suppose we have the following functions:

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

Esito Async exposes a `zip` method to compute two independent execution in parallel:

```kotlin
suspend fun fetchIfo(userId: Int): Result<DataForUI, FeatureError> =
	zip(
		{ getUserFullName(userId) },
		{ getUserStatus(userId) },
		::DataForUI //syntactic sugar for constructor
		).invoke()
```

#### Result Extensions

Esito Async allows you to zip different Results to obtain a new one, for example:

```kotlin
val first = Result.success<Int, String>(1)
val second = Result.success<Int, String>(42)

val result = first.zip(second) { a, b -> a + b }
// Result is a Success with value of 43
```

```kotlin
val first = Result.failure<Int, String>("error")
val second = Result.success<Int, String>(42)

val result = first.zip(second) { a, b -> a + b }
// Result is a Failure with "error" value
```