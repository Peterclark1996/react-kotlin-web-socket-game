import arrow.core.Either
import arrow.core.flatMap
import events.Event
import events.Receivable
import events.inbound.*
import events.outbound.OutboundRoomUsersUpdated
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import state.Connection
import state.ServerState

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(WebSockets)
    routing {
        val serverState = ServerState()
        println("Server started")

        webSocket("/room") {
            val currentConnection = Connection(this)
            serverState.addConnection(currentConnection)
            try {
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val frameText = frame.readText()
                    decodeJsonStringToEvent(frameText).flatMap { event ->
                        processEvent(event, currentConnection, serverState)
                    }.mapLeft {
                        println("ERROR: $it")
                    }
                }
            } catch (e: Exception) {
                println("ERROR: $e")
            } finally {
                serverState.disconnectConnection(currentConnection)
            }
        }
    }
}

suspend fun processEvent(
    event: Event,
    currentConnection: Connection,
    serverState: ServerState
): Either<Error, Unit> =
    when (event.type) {
        "InboundUserTriedToCreateRoom" ->
            decodeAndProcessEvent<InboundUserTriedToCreateRoom>(event.jsonData, currentConnection, serverState)
        "InboundUserTriedToJoinRoom" ->
            decodeAndProcessEvent<InboundUserTriedToJoinRoom>(event.jsonData, currentConnection, serverState)
        "InboundRequestRoomUsers" ->
            decodeAndProcessEvent<InboundRequestRoomUsers>(event.jsonData, currentConnection, serverState)
        "InboundUpdatePressedKey" ->
            decodeAndProcessEvent<InboundUpdatePressedKey>(event.jsonData, currentConnection, serverState)
        "InboundUserStartedGame" ->
            decodeAndProcessEvent<InboundUserStartedGame>(event.jsonData, currentConnection, serverState)
        else -> Error("Event type not recognised: ${event.type}").toLeft()
    }

suspend inline fun <reified T : Receivable> decodeAndProcessEvent(
    eventJsonData: String,
    currentConnection: Connection,
    serverState: ServerState
) =
    decodeJsonStringToEventData<T>(eventJsonData).flatMap {
        it.onReceive(
            currentConnection,
            serverState
        )
    }

suspend fun ServerState.disconnectConnection(connection: Connection) {
    this.removeConnection(connection)
    val room = this.getRooms().find { it.roomCode == connection.roomCode }
    if (room != null) {
        this.sendToRoom(
            room.roomCode,
            OutboundRoomUsersUpdated.serializer(),
            OutboundRoomUsersUpdated(
                this.getAllUsersInRoom(room.roomCode)
            )
        ).mapLeft { println("ERROR: $it") }

        if (this.getAllConnectionsInRoom(room.roomCode).isEmpty()) {
            this.removeRoom(room)
        }
    }
}