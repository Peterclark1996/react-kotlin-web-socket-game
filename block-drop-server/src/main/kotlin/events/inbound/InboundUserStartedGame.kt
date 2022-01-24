package events.inbound

import arrow.core.Either
import events.Receivable
import kotlinx.serialization.Serializable
import startGame
import state.Connection
import state.ServerState
import toEither

@Serializable
class InboundUserStartedGame : Receivable {
    override suspend fun onReceive(currentConnection: Connection, serverState: ServerState): Either<Error, Unit> =
        serverState.getRooms().find { it.roomCode == currentConnection.roomCode }
            .toEither()
            .map { serverState.startGame(it.roomCode) }
            .mapLeft { Error("Cannot start game") }
}