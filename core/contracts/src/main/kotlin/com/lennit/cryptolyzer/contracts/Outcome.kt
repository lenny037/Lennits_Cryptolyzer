package com.lennit.cryptolyzer.contracts

/**
 * Explicit success/failure channel for every fallible boundary in the platform.
 *
 * Deliberately not kotlin.Result: this type carries a domain [PlatformError] rather than an
 * arbitrary Throwable, is exhaustively matchable, and cannot be silently discarded by a
 * `runCatching` swallow. Exceptions remain reserved for programming defects.
 */
public sealed interface Outcome<out T> {

    public data class Success<out T>(val value: T) : Outcome<T>

    public data class Failure(val error: PlatformError) : Outcome<Nothing>

    public val isSuccess: Boolean get() = this is Success

    public fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    public fun errorOrNull(): PlatformError? = when (this) {
        is Success -> null
        is Failure -> error
    }

    public companion object {
        public fun <T> success(value: T): Outcome<T> = Success(value)
        public fun failure(error: PlatformError): Outcome<Nothing> = Failure(error)
    }
}

public inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

public inline fun <T, R> Outcome<T>.flatMap(transform: (T) -> Outcome<R>): Outcome<R> = when (this) {
    is Outcome.Success -> transform(value)
    is Outcome.Failure -> this
}

public fun <T> Outcome<T>.getOrThrow(): T = when (this) {
    is Outcome.Success -> value
    is Outcome.Failure -> throw IllegalStateException("Outcome failed: $error")
}
