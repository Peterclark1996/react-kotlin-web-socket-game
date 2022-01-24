package events.inbound

import arrow.core.Either
import events.Receivable
import kotlinx.serialization.Serializable
import state.Connection
import state.KeyTypes
import state.ServerState
import toRight

@Serializable
data class InboundUpdatePressedKey(val pressedKey: KeyTypes): Receivable {
    override suspend fun onReceive(currentConnection: Connection, serverState: ServerState): Either<Error, Unit> {
        currentConnection.pressedKey = this.pressedKey
        return Unit.toRight()
    }
}