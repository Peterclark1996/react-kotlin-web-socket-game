package state

import events.outbound.OutboundGameStateUpdated
import logic.getAllConnectionsInRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logic.getNextGameState
import sendToRoom

const val DELAY_BETWEEN_TICKS: Long = 200

class Room(val roomCode: String) {
    private var running = false
    private var currentGameState: GameState? = null

    fun start(serverState: ServerState) {
        currentGameState = GameState.createNew(serverState.getAllConnectionsInRoom(roomCode))

        running = true
        CoroutineScope(Job()).launch {
            while (running) {
                delay(DELAY_BETWEEN_TICKS)

                currentGameState = currentGameState?.getNextGameState()
                val nullSafeCurrentGameState = currentGameState
                if (nullSafeCurrentGameState == null) {
                    running = false
                } else {
                    serverState.sendToRoom(
                        roomCode,
                        OutboundGameStateUpdated.serializer(),
                        OutboundGameStateUpdated(
                            nullSafeCurrentGameState.currentTick,
                            nullSafeCurrentGameState.getTilesWithBlocks()
                        )
                    )
                }
            }
        }
    }

    fun stop() {
        running = false
    }
}