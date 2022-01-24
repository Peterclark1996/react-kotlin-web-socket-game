package events.inbound

import arrow.core.Either
import events.Receivable
import events.outbound.OutboundRoomUsersUpdated
import getAllUsersInRoom
import kotlinx.serialization.Serializable
import mapToUnit
import sendEvent
import state.Connection
import state.ServerState
import toEither

@Serializable
class InboundRequestRoomUsers: Receivable {
    override suspend fun onReceive(currentConnection: Connection, serverState: ServerState): Either<Error, Unit> =
        currentConnection.roomCode.toEither().map { room ->
            currentConnection.sendEvent(
                OutboundRoomUsersUpdated.serializer(),
                OutboundRoomUsersUpdated(
                    serverState.getAllUsersInRoom(room)
                )
            )
        }.mapToUnit()
}