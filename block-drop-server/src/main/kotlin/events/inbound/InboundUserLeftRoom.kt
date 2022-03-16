package events.inbound

import arrow.core.Either
import events.Receivable
import events.outbound.OutboundRoomUsersUpdated
import kotlinx.serialization.Serializable
import logic.getAllUsersInRoom
import logic.sendToRoom
import state.Connection
import state.ServerState
import toRight

@Serializable
class InboundUserLeftRoom : Receivable {
    override suspend fun onReceive(currentConnection: Connection, serverState: ServerState): Either<Error, Unit> =
        Unit.toRight<Error, Unit>().apply {
            val roomCode = currentConnection.roomCode
            if (roomCode != null) {
                currentConnection.roomCode = null
                currentConnection.username = null
                serverState.sendToRoom(
                    roomCode,
                    OutboundRoomUsersUpdated.serializer(),
                    OutboundRoomUsersUpdated(
                        serverState.getAllUsersInRoom(roomCode)
                    )
                )
            }
        }
}