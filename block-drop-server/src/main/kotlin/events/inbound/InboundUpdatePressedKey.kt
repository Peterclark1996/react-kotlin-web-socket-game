package events.inbound

import arrow.core.Either
import events.Receivable
import kotlinx.serialization.Serializable
import state.*
import toRight

@Serializable
data class InboundUpdatePressedKey(
    val pressingLeft: Boolean,
    val pressingRight: Boolean,
    val pressingDown: Boolean,
    val pressingRotateLeft: Boolean,
    val pressingRotateRight: Boolean
) : Receivable {
    override suspend fun onReceive(currentConnection: Connection, serverState: ServerState): Either<Error, Unit> {

        if (!currentConnection.pressingRotateLeft && this.pressingRotateLeft) {
            serverState.getRooms().find { it.roomCode == currentConnection.roomCode }
                ?.triggerPlayerStartMovement(
                    currentConnection,
                    serverState,
                    false,
                    ::canBlockRotateLeft,
                    ::rotateBlockClockwise
                )
        }

        if (!currentConnection.pressingRotateRight && this.pressingRotateRight) {
            serverState.getRooms().find { it.roomCode == currentConnection.roomCode }
                ?.triggerPlayerStartMovement(
                    currentConnection,
                    serverState,
                    false,
                    ::canBlockRotateRight,
                    ::rotateBlockAntiClockwise
                )
        }

        if (!currentConnection.pressingLeft && this.pressingLeft) {
            serverState.getRooms().find { it.roomCode == currentConnection.roomCode }
                ?.triggerPlayerStartMovement(
                    currentConnection,
                    serverState,
                    true,
                    ::canBlockMoveLeft,
                    ::translateBlockLeft
                )
        }

        if (!currentConnection.pressingRight && this.pressingRight) {
            serverState.getRooms().find { it.roomCode == currentConnection.roomCode }
                ?.triggerPlayerStartMovement(
                    currentConnection,
                    serverState,
                    true,
                    ::canBlockMoveRight,
                    ::translateBlockRight
                )
        }

        currentConnection.pressingLeft = this.pressingLeft
        currentConnection.pressingRight = this.pressingRight
        currentConnection.pressingDown = this.pressingDown
        currentConnection.pressingRotateLeft = this.pressingRotateLeft
        currentConnection.pressingRotateRight = this.pressingRotateRight

        return Unit.toRight()
    }
}