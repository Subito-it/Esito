#Recipes

Here you can find common usages of Esito that can be used for daily tasks:

### Creation

#### How should I create a Result?

If you already have the data or the error, and you just need to put it in a `Result` you can use factory methods on the companion object:

```kotlin
val success = Result.success<Int, String>(42)

val failure = Result.failure<Int, String>("error")

```

If you need to wrap an existing method throwing exceptions, you could use the `runCatching` method:

```kotlin
val result: Result<Int, Throwable> = runCatching { "notAnInt".toInt() }
```

if you need support for suspending methods, you can use `runCatchingSuspend`.


#### How should I extract data from a Result?

For the following examples suppose we have to handle the result of this method:

```kotlin
fun tryToParse(string: String): Result<Int, Throwable> = ...
```

The best way to handle the data inside a Result is to use the `fold` method:

```kotlin
val data: String = tryToParse("input").fold(
        onSuccess = { "Your number is $it" },
        onFailure = { "Ops, something went wrong" }
)
// data is "Your number is 42"
```

Please note the type of data is `String`, not `Result`, usually this method is used at the end of a composition, handling both success and failure.

If you don't need the error value, you can use the `getOrNull` method:

```kotlin
val data: Int? = tryToParse("42").getOrNull()
// data is 42
```

If you are happy with a default value when an error occurs, you could use the `getOrDefault` method instead:

```kotlin
val data: Int = tryToParse("42").getOrDefault(Int.MIN_VALUE)
// data is 42
```

### How should I manipulate the content of a Result?

If you need to change the content of a Result, you can use both `map` and `mapError`:

```kotlin
val ok: Result<Int, Throwable> = tryToParse("42").map { it * 2 }
// ok is Success(value=84)

val fail: Result<Int, String> = tryToParse("Invalid").mapError { it.stackTraceToString() }
// data is "Failure(error=java.lang.NumberFormatException: For input string: "Invalid" ...."
```

### How should I apply side effects?

If you need to access the value or the error of a Result to apply some side effect you can use `onSuccess` and `onFailure`:

```kotlin
var cachedValue: Int?
val ok: Result<Int, Throwable> = tryToParse("42").onSuccess { cachedValue = it }

val failure: Result<Int, Throwable> = tryToParse("Invalid").onFailure { Log.d(it) }
```

### How should I chain different methods returning a Result?

To run in sequence methods returning a Result you could use `flatMap`.

Let's define some classes and methods for an example:

```kotlin
data class User(
    val id: String,
    val name: String
)
```

```kotlin
sealed class GetFirstFollowerError {
    data class UserNotFound(val userId: String) : GetFirstFollowerError()
    object NoFollowersForUser : GetFirstFollowerError()
}
```

We have our representation of the User, and we have defined how our execution could fail. Now suppose we have the following functions:

```kotlin
fun getUserFollowers(id: String): Result<List<User>, GetFirstFollowerError> = ...

fun getUserDetails(id: String): Result<User, GetFirstFollowerError> = ...
```

For the sake of the example we don't care how they are implemented, we just know that one is used to get a list of followers for a given user, and the other one is used to get the user details.

We can now chain these functions to get the details of the first follower for a user:

```kotlin
val firstFollower: Result<User, GetFirstFollowerError> =
getUserFollowers("user").flatMap { getUserDetails(it.first().id) }
```

Let's complicate the example to see how we can chain methods also to handle errors.

If getUserFollowers fails, we would like to retrieve followers from another platform and continue the execution flow with that list of followers:

```kotlin
fun getUserFollowersSecondPlatform(id: String): Result<List<User>, GetFirstFollowerError> = ...
```

Thanks to `flatRecover` we can easily implement this new requirement:

```kotlin
val user = "user"
val firstFollower: Result<User, GetFirstFollowerError> =
getUserFollowers(user)
	.flatRecover {
		getUserFollowersSecondPlatform(user)
	}
	.flatMap {
		getUserDetails(it.first().id)
	}
```
