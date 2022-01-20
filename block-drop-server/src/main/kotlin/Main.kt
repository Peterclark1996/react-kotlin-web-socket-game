import arrow.core.Either
import arrow.core.flatMap
import events.Event
import events.inbound.InboundUpdatePressedKey
import events.inbound.InboundUserTriedToJoinRoom
import events.inbound.InboundUserStartedGame
import events.inbound.InboundUserTriedToCreateRoom
import events.outbound.*
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
        val rooms = Collections.synchronizedSet<Room?>(LinkedHashSet())

        webSocket("/room") {
            val currentConnection = Connection(this)
            connections += currentConnection
            try {
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val frameText = frame.readText()
                    decodeJsonStringToEvent(frameText).flatMap { event ->
                        processEvent(event, currentConnection, connections, rooms)
                    }.mapLeft { println("ERROR: $it") }
                }
            } catch (e: Exception) {
                println("ERROR: $e")
            } finally {
                connections -= currentConnection
                val room = rooms.find { it.roomCode == currentConnection.roomCode }
                if (room != null) {
                    connections.sendToRoom(
                        room.roomCode,
                        OutboundRoomUsersUpdated.serializer(),
                        OutboundRoomUsersUpdated(
                            connections.getAllUsersInRoom(room.roomCode)
                        )
                    ).mapLeft { println("ERROR: $it") }

                    if(connections.getAllConnectionsInRoom(room.roomCode).isEmpty()){
                        rooms -= room
                    }
                }
            }
        }
    }
}

suspend fun processEvent(
    event: Event,
    currentConnection: Connection,
    connections: MutableSet<Connection>,
    rooms: MutableSet<Room>
): Either<Error, Unit> =
    when (event.type) {
        "InboundUserTriedToCreateRoom" ->
            decodeJsonStringToEventData<InboundUserTriedToCreateRoom>(event.jsonData).flatMap { eventData ->
                val newRoom = Room(rooms.getUnusedRoomCode())
                rooms += newRoom
                currentConnection.roomCode = newRoom.roomCode
                currentConnection.username = eventData.username
                currentConnection.sendEvent(
                    OutboundUserTriedToCreateRoomSuccess.serializer(),
                    OutboundUserTriedToCreateRoomSuccess(newRoom.roomCode, eventData.username)
                )
            }
        "InboundUserTriedToJoinRoom" ->
            decodeJsonStringToEventData<InboundUserTriedToJoinRoom>(event.jsonData).flatMap { eventData ->
                if (connections.any { it.roomCode == eventData.room && it.username?.lowercase() == eventData.username.lowercase() }) {
                    currentConnection.sendEvent(
                        OutboundUserTriedToJoinRoomFailure.serializer(),
                        OutboundUserTriedToJoinRoomFailure("Name already taken")
                    )
                } else if (!rooms.any { it.roomCode == eventData.room }) {
                    currentConnection.sendEvent(
                        OutboundUserTriedToJoinRoomFailure.serializer(),
                        OutboundUserTriedToJoinRoomFailure("Room not found")
                    )
                } else {
                    currentConnection.roomCode = eventData.room
                    currentConnection.username = eventData.username
                    currentConnection.sendEvent(
                        OutboundUserTriedToJoinRoomSuccess.serializer(),
                        OutboundUserTriedToJoinRoomSuccess(eventData.room, eventData.username)
                    ).flatMap {
                        connections.sendToRoom(
                            eventData.room,
                            OutboundRoomUsersUpdated.serializer(),
                            OutboundRoomUsersUpdated(
                                connections.getAllUsersInRoom(eventData.room)
                            )
                        )
                    }.mapToUnit()
                }
            }
        "InboundRequestRoomUsers" ->
            currentConnection.roomCode.toEither().map { room ->
                currentConnection.sendEvent(
                    OutboundRoomUsersUpdated.serializer(),
                    OutboundRoomUsersUpdated(
                        connections.getAllUsersInRoom(room)
                    )
                )
            }.mapToUnit()
        "InboundUpdatePressedKey" ->
            decodeJsonStringToEventData<InboundUpdatePressedKey>(event.jsonData).map { eventData ->
                currentConnection.pressedKey = eventData.pressedKey
            }
        "InboundUserStartedGame" ->
            decodeJsonStringToEventData<InboundUserStartedGame>(event.jsonData).flatMap { _ ->
                rooms.find { it.roomCode == currentConnection.roomCode }
                    .toEither()
                    .map { it.startGame(connections) }
                    .mapLeft { Error("Cannot start game") }
            }
        else -> Error("Event type not recognised: ${event.type}").toLeft()
    }