import arrow.core.Either
import arrow.core.flatMap
import events.Event
import events.inbound.InboundUserJoinedRoom
import events.outbound.OutboundRoomUsersUpdated
import events.outbound.OutboundUserJoinedRoomFailure
import events.outbound.OutboundUserJoinedRoomSuccess
import events.sendEvent
import events.sendToRoom
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
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
                val room = currentConnection.room
                if(!room.isNullOrBlank()){
                    connections.sendToRoom(
                        room,
                        OutboundRoomUsersUpdated.serializer(),
                        OutboundRoomUsersUpdated(
                            connections
                                .filter { c -> c.room == currentConnection.room }
                                .mapNotNull { c -> c.username }
                        )
                    ).mapLeft { println("ERROR: $it") }
                }
            }
        }
    }
}

suspend fun processEvent(
    event: Event,
    currentConnection: Connection,
    connections: MutableSet<Connection>
): Either<Error, Unit> =
    when(event.type) {
        "InboundUserJoinedRoom" ->
            decodeJsonStringToEventData<InboundUserJoinedRoom>(event.jsonData).flatMap { eventData ->
                if(connections.any{c -> c.room == eventData.room && c.username?.lowercase() == eventData.username.lowercase()}){
                    return currentConnection.sendEvent(
                        OutboundUserJoinedRoomFailure.serializer(),
                        OutboundUserJoinedRoomFailure("Name already taken")
                    )
                }
                currentConnection.room = eventData.room
                currentConnection.username = eventData.username
                return currentConnection.sendEvent(
                    OutboundUserJoinedRoomSuccess.serializer(),
                    OutboundUserJoinedRoomSuccess(eventData.room, eventData.username)
                ).flatMap {
                    connections.sendToRoom(
                        eventData.room,
                        OutboundRoomUsersUpdated.serializer(),
                        OutboundRoomUsersUpdated(
                            connections
                                .filter { c -> c.room == eventData.room }
                                .mapNotNull { c -> c.username }
                        )
                    )
                }.map { }
            }
        else -> Error("Event type not recognised: ${event.type}").asLeft()
    }