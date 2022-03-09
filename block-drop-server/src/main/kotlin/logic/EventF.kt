package logic

import state.Connection
import arrow.core.Either
import events.Event
import flatten
import io.ktor.http.cio.websocket.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import state.ServerState
import toLeft
import toRight

suspend fun <T> Connection.sendEvent(
    serializer: KSerializer<T>,
    eventData: T
): Either<Error, Unit> {
    val eventType = eventData!!::class.simpleName
    if(eventType.isNullOrEmpty()) return Error("Failed to get event name for event type $eventData").toLeft()

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
    return Unit.toRight()
}

suspend fun <T> ServerState.sendToRoom(
    room: String,
    serializer: KSerializer<T>,
    eventData: T
) = this.getAllConnectionsInRoom(room)
    .map { it.sendEvent(serializer, eventData) }
    .flatten()