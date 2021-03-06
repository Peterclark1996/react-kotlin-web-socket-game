package events.inbound

import arrow.core.Either
import arrow.core.flatMap
import events.Receivable
import events.outbound.OutboundRoomUsersUpdated
import events.outbound.OutboundUserTriedToJoinRoomFailure
import events.outbound.OutboundUserTriedToJoinRoomSuccess
import logic.getAllUsersInRoom
import kotlinx.serialization.Serializable
import mapToUnit
import logic.sendEvent
import logic.sendToRoom
import state.Connection
import state.ServerState

@Serializable
data class InboundUserTriedToJoinRoom(val room: String, val username: String): Receivable {
    override suspend fun onReceive(currentConnection: Connection, serverState: ServerState): Either<Error, Unit> =
        if (this.username == "") {
            currentConnection.sendEvent(
                OutboundUserTriedToJoinRoomFailure.serializer(),
                OutboundUserTriedToJoinRoomFailure("Invalid username")
            )
        } else if (serverState.getConnections().any { it.roomCode == this.room && it.username?.lowercase() == this.username.lowercase() }) {
            currentConnection.sendEvent(
                OutboundUserTriedToJoinRoomFailure.serializer(),
                OutboundUserTriedToJoinRoomFailure("Name already taken")
            )
        } else if (!serverState.getRooms().any { it.roomCode == this.room }) {
            currentConnection.sendEvent(
                OutboundUserTriedToJoinRoomFailure.serializer(),
                OutboundUserTriedToJoinRoomFailure("Room not found")
            )
        } else {
            currentConnection.roomCode = this.room
            currentConnection.username = this.username
            currentConnection.sendEvent(
                OutboundUserTriedToJoinRoomSuccess.serializer(),
                OutboundUserTriedToJoinRoomSuccess(this.room, this.username)
            ).flatMap {
                serverState.sendToRoom(
                    this.room,
                    OutboundRoomUsersUpdated.serializer(),
                    OutboundRoomUsersUpdated(
                        serverState.getAllUsersInRoom(this.room)
                    )
                )
            }.mapToUnit()
        }
}