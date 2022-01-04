import arrow.core.Either
import arrow.core.left
import arrow.core.right

fun <TL, TR> TL.asLeft(): Either<TL, TR> = this.left()
fun <TL, TR> TR.asRight(): Either<TL, TR> = this.right()

fun <T> tryCatch(f: () -> T): Either<Error, T> =
    try {
        f().asRight()
    } catch (e: Throwable) {
        Error(e).asLeft()
    }