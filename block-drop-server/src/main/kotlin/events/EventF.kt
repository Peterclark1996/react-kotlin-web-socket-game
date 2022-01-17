package events

import Connection
import arrow.core.Either
import asLeft
import asRight
import flatten
import getAllConnectionsInRoom
import io.ktor.http.cio.websocket.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

suspend fun <T> Connection.sendEvent(
    serializer: KSerializer<T>,
    eventData: T
): Either<Error, Unit> {
    val eventType = eventData!!::class.simpleName
    if(eventType.isNullOrEmpty()) return Error("Failed to get event name for event type $eventData").asLeft()

    this.session.send(
        Json.encodeToString(
            Event.serializer(),
            Event(
                eventType,
                Json.encodeToString(
                    serializer,
                    eventData
                )
            )
        )
    )
    return Unit.asRight()
}

suspend fun <T> Set<Connection>.sendToRoom(
    room: String,
    serializer: KSerializer<T>,
    eventData: T
) = this.getAllConnectionsInRoom(room)
    .map { it.sendEvent(serializer, eventData) }
    .flatten()