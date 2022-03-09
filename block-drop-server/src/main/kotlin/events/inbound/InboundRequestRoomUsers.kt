package events.inbound

import arrow.core.Either
import arrow.core.flatMap
import events.Receivable
import events.outbound.OutboundRoomUsersUpdated
import logic.getAllUsersInRoom
import kotlinx.serialization.Serializable
import mapToUnit
import logic.sendEvent
import state.Connection
import state.ServerState
import toEither
import toRight

@Serializable
class InboundRequestRoomUsers: Receivable {
    override suspend fun onReceive(currentConnection: Connection, serverState: ServerState): Either<Error, Unit> =
        currentConnection.roomCode?.let { room ->
            currentConnection.sendEvent(
                OutboundRoomUsersUpdated.serializer(),
                OutboundRoomUsersUpdated(
                    serverState.getAllUsersInRoom(room)
                )
            )
        } ?: Unit.toRight()
}