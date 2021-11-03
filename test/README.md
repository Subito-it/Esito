# Esito

Esito test utilities.

## Installation

```groovy
repositories {
	maven { url 'https://jitpack.io' }
}

dependencies {
    testImplementation("com.github.Subito-it.Esito:test:(insert latest version)")
}
```

### Usage

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