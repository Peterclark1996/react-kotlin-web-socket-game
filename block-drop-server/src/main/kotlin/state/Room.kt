package state

import events.outbound.OutboundGameStateUpdated
import getAllConnectionsInRoom
import kotlinx.coroutines.*
import sendToRoom

class Room(val roomCode: String) {
    private var running = false
    private var tick = 0
    private var currentGameState: GameState? = null

    fun start(serverState: ServerState) {
        currentGameState = GameState.createNew(serverState.getAllConnectionsInRoom(roomCode))

        running = true
        CoroutineScope(Job()).launch {
            while (running) {
                delay(1000)

                val tilesState = currentGameState?.tiles
                if (tilesState == null) {
                    running = false
                } else {
                    serverState.sendToRoom(
                        roomCode,
                        OutboundGameStateUpdated.serializer(),
                        OutboundGameStateUpdated(tick, tilesState)
                    )
                    tick++
                }
            }
        }
    }

    fun stop() {
        running = false
    }
}