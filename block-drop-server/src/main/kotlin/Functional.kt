import arrow.core.Either
import arrow.core.left
import arrow.core.right

fun <TLeft, TRight> TLeft.asLeft(): Either<TLeft, TRight> = this.left()

fun <TLeft, TRight> TRight.asRight(): Either<TLeft, TRight> = this.right()

fun <T> tryCatch(func: () -> T): Either<Error, T> =
    try { func().asRight() }
    catch (e: Throwable) { Error(e).asLeft() }

fun <T> Iterable<Either<Error, T>>.flatten(): Either<Error, Iterable<T>> =
    this.filterIsInstance<Either.Left<Error>>().let { lefts ->
        if (lefts.any()) lefts.map { it.value }.reduce { a, error -> Error(error.message, a) }.asLeft()
        else this.filterIsInstance<Either.Right<T>>().map { it.value }.asRight()
    }