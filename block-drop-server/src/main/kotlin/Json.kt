import arrow.core.Either
import events.Event
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

fun decodeJsonStringToEvent(eventString: String) =
    tryCatch { Json.decodeFromString(Event.serializer(), eventString) }
        .mapLeft { e -> Error("Failed to parse event.", e) }

inline fun <reified T>decodeJsonStringToEventData(eventData: String): Either<Error, T> =
    tryCatch { Json.decodeFromString(serializer<T>(), eventData) }
        .mapLeft { e -> Error("Failed to parse event data.", e) }