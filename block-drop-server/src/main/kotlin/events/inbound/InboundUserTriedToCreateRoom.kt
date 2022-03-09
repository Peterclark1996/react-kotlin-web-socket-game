package events.inbound

import state.Connection
import state.Room
import arrow.core.Either
import events.Receivable
import events.outbound.OutboundUserTriedToCreateRoomFailure
import events.outbound.OutboundUserTriedToCreateRoomSuccess
import logic.sendEvent
import logic.getUnusedRoomCode
import kotlinx.serialization.*
import state.ServerState

@Serializable
data class InboundUserTriedToCreateRoom(val username: String): Receivable {
    override suspend fun onReceive(currentConnection: Connection, serverState: ServerState): Either<Error, Unit> {
        if(this.username == ""){
            return currentConnection.sendEvent(
                OutboundUserTriedToCreateRoomFailure.serializer(),
                OutboundUserTriedToCreateRoomFailure("Invalid username")
            )
        }

        val newRoom = Room(serverState.getUnusedRoomCode())
        serverState.addRoom(newRoom)
        currentConnection.roomCode = newRoom.roomCode
        currentConnection.username = this.username
        return currentConnection.sendEvent(
            OutboundUserTriedToCreateRoomSuccess.serializer(),
            OutboundUserTriedToCreateRoomSuccess(newRoom.roomCode, this.username)
        )
    }
}