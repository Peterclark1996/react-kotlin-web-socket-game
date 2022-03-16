package state

import events.outbound.OutboundGameStateUpdated
import events.outbound.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logic.*

const val DELAY_BETWEEN_TICKS: Long = 100

class Room(val roomCode: String) {
    private var running = false
    private var currentGameState: GameState? = null

    fun start(serverState: ServerState) {
        val players = serverState.getAllConnectionsInRoom(roomCode)
        val minimumSize = (players.size * 5) + 1
        currentGameState =
            createNewGameState(players, 20, maxOf(minimumSize, 12)).orNull()
        if (currentGameState == null) {
            return
        }

        running = true
        CoroutineScope(Job()).launch {
            while (running) {
                delay(DELAY_BETWEEN_TICKS)

                currentGameState = currentGameState?.getNextGameState()
                updateClientsWithGameState(serverState)
            }
        }
    }

    fun stop() {
        running = false
    }

    suspend fun triggerPlayerStartMovement(
        connection: Connection,
        serverState: ServerState,
        disableHorizontalMovement: Boolean,
        moveF: (Block) -> Block
    ) {
        val nullSafeGameState = currentGameState ?: return
        currentGameState = updateBlockPositionForConnection(
            nullSafeGameState,
            connection,
            disableHorizontalMovement,
            moveF
        )
        updateClientsWithGameState(serverState)
    }

    private suspend fun updateClientsWithGameState(serverState: ServerState) {
        val nullSafeCurrentGameState = currentGameState
        if (nullSafeCurrentGameState == null) {
            running = false
        } else {
            serverState.sendToRoom(
                roomCode,
                OutboundGameStateUpdated.serializer(),
                OutboundGameStateUpdated(
                    nullSafeCurrentGameState.currentTick,
                    nullSafeCurrentGameState.getTilesWithBlocks(),
                    nullSafeCurrentGameState.players.map {
                        Player(it.id, it.connection.username ?: "Unknown Player", it.score, it.isDead)
                    }.toSet()
                )
            )
        }
    }
}