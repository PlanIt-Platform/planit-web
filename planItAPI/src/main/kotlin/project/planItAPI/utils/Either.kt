package project.planItAPI.utils

/**
 * Sealed class representing either a result of type [R] or an error of type [L].
 *
 * @param L The type of the left (error) component.
 * @param R The type of the right (result) component.
 */
sealed class Either<out L, out R> {

    /**
     * Represents the left (error) component of [Either].
     *
     * @param value The value of the left component.
     */
    data class Left<out L>(val value: L) : Either<L, Nothing>()

    /**
     * Represents the right (result) component of [Either].
     *
     * @param value The value of the right component.
     */
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

/**
 * Creates an [Either.Right] instance representing a successful result.
 *
 * @param value The result value.
 * @return An [Either.Right] instance containing the result value.
 */
fun <R> success(value: R) = Either.Right(value)

/**
 * Creates an [Either.Left] instance representing a failure/error.
 *
 * @param error The error value.
 * @return An [Either.Left] instance containing the error value.
 */
fun <L> failure(error: L) = Either.Left(error)

/**
 * Type alias for representing a successful result using [Either].
 *
 * @param S The type of the successful result.
 */
typealias Success<S> = Either.Right<S>

/**
 * Type alias for representing a failure/error using [Either].
 *
 * @param F The type of the failure/error.
 */
typealias Failure<F> = Either.Left<F>