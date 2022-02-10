package events.inbound

import arrow.core.Either
import events.Receivable
import kotlinx.serialization.Serializable
import state.Connection
import state.ServerState
import toRight

@Serializable
data class InboundUpdatePressedKey(
    val pressingLeft: Boolean,
    val pressingRight: Boolean,
    val pressingDown: Boolean,
    val pressingRotateLeft: Boolean,
    val pressingRotateRight: Boolean
): Receivable {
    override suspend fun onReceive(currentConnection: Connection, serverState: ServerState): Either<Error, Unit> {
        currentConnection.pressingLeft = this.pressingLeft
        currentConnection.pressingRight = this.pressingRight
        currentConnection.pressingDown = this.pressingDown
        currentConnection.pressingRotateLeft = this.pressingRotateLeft
        currentConnection.pressingRotateRight = this.pressingRotateRight
        return Unit.toRight()
    }
}