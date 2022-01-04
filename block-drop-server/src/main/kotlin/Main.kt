import arrow.core.flatMap
import events.*
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(WebSockets)
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/room") {
            val currentConnection = Connection(this)
            connections += currentConnection
            try {
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val frameText = frame.readText()
                    decodeJsonStringToEvent(frameText).flatMap { event ->
                        processEvent(event, currentConnection, connections)
                    }.mapLeft { println("ERROR: $it") }
                }
            } catch (e: Exception) {
                println("ERROR: $e")
            } finally {
                println("Removing $currentConnection!")
                connections -= currentConnection
                if(!currentConnection.room.isNullOrBlank()){
                    connections.forEach {
                        it.session.send(
                            Json.encodeToString(
                                Event.serializer(),
                                Event(
                                    EventType.OUTBOUND_ROOM_USERS_UPDATED,
                                    Json.encodeToString(
                                        OutboundRoomUsersUpdated.serializer(),
                                        OutboundRoomUsersUpdated(
                                            connections
                                                .filter { c -> c.room == currentConnection.room }
                                                .mapNotNull { c -> c.username }
                                        )
                                    )
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

suspend fun processEvent(
    event: Event,
    currentConnection: Connection,
    connections: MutableSet<Connection>
) =
    when (event.type) {
        EventType.INBOUND_USER_JOINED_ROOM -> {
            decodeJsonStringToEventData<InboundUserJoinedRoom>(event.jsonData).map { eventData ->
                currentConnection.room = eventData.roomId
                currentConnection.username = eventData.username
                currentConnection.session.send(
                    Json.encodeToString(
                        Event.serializer(),
                        Event(
                            EventType.OUTBOUND_USER_JOINED_ROOM_RESULT,
                            Json.encodeToString(
                                OutboundUserJoinedRoomResult.serializer(),
                                OutboundUserJoinedRoomResult(eventData.roomId, eventData.username, true)
                            )
                        )
                    )
                )
                connections.forEach {
                    it.session.send(
                        Json.encodeToString(
                            Event.serializer(),
                            Event(
                                EventType.OUTBOUND_ROOM_USERS_UPDATED,
                                Json.encodeToString(
                                    OutboundRoomUsersUpdated.serializer(),
                                    OutboundRoomUsersUpdated(
                                        connections
                                            .filter { c -> c.room == eventData.roomId }
                                            .mapNotNull { c -> c.username }
                                    )
                                )
                            )
                        )
                    )
                }
            }
        }
        else -> Error("Event type not recognised: ${event.type}").asLeft()
    }