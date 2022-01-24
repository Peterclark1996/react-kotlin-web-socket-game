import arrow.core.Either
import arrow.core.left
import arrow.core.right

fun <TLeft, TRight> TLeft.toLeft(): Either<TLeft, TRight> = this.left()

fun <TLeft, TRight> TRight.toRight(): Either<TLeft, TRight> = this.right()

fun <T> Either<Error, T>.mapToUnit() = this.map{ }

fun <T> tryCatch(func: () -> T): Either<Error, T> =
    try { func().toRight() }
    catch (e: Throwable) { Error(e).toLeft() }

fun <T> Iterable<Either<Error, T>>.flatten(): Either<Error, Iterable<T>> =
    this.filterIsInstance<Either.Left<Error>>().let { lefts ->
        if (lefts.any()) lefts.map { it.value }.reduce { a, error -> Error(error.message, a) }.toLeft()
        else this.filterIsInstance<Either.Right<T>>().map { it.value }.toRight()
    }

fun <T> T?.toEither() = this?.toRight<Error, T>() ?: Error("No value").toLeft()