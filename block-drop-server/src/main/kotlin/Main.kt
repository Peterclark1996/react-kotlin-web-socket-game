import arrow.core.flatMap
import events.*
import events.inbound.InboundUserJoinedRoom
import events.outbound.OutboundRoomUsersUpdated
import events.outbound.OutboundUserJoinedRoomResult
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.KSerializer
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
                connections -= currentConnection
                if(!currentConnection.room.isNullOrBlank()){
                    connections.forEach {
                        it.session.sendEvent(
                            OutboundRoomUsersUpdated.serializer(),
                            OutboundRoomUsersUpdated(
                                connections
                                    .filter { c -> c.room == currentConnection.room }
                                    .mapNotNull { c -> c.username }
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
    when(event.type) {
        "InboundUserJoinedRoom" -> {
            decodeJsonStringToEventData<InboundUserJoinedRoom>(event.jsonData).map { eventData ->
                currentConnection.room = eventData.roomId
                currentConnection.username = eventData.username
                currentConnection.session.sendEvent(
                    OutboundUserJoinedRoomResult.serializer(),
                    OutboundUserJoinedRoomResult(eventData.roomId, eventData.username, true)
                )
                connections.forEach {
                    it.session.sendEvent(
                        OutboundRoomUsersUpdated.serializer(),
                        OutboundRoomUsersUpdated(
                            connections
                                .filter { c -> c.room == eventData.roomId }
                                .mapNotNull { c -> c.username }
                        )
                    )
                }
            }
        }
        else -> Error("Event type not recognised: ${event.type}").asLeft()
    }

suspend fun <T>DefaultWebSocketSession.sendEvent(
    serializer: KSerializer<T>,
    eventData: T
) {
    val eventType = eventData!!::class.simpleName ?: "UnknownEvent"
    this.send(
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
}