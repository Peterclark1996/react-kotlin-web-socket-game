package events

import arrow.core.Either
import state.Connection
import state.ServerState

interface Receivable {
    suspend fun onReceive(currentConnection: Connection, serverState: ServerState): Either<Error, Unit>
}