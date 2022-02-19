package logic

import arrow.core.flatMap
import events.outbound.OutboundGameStarted
import mapToUnit
import sendToRoom
import state.*
import toEither
import java.util.*

fun ServerState.getAllUsersInRoom(room: String) =
    this.getAllConnectionsInRoom(room)
        .map { c -> c.username }
        .mapNotNull { it }

fun ServerState.getAllConnectionsInRoom(room: String) =
    this.getConnections().filter { c -> c.roomCode == room }

fun ServerState.getUnusedRoomCode(): String {
    var randomString = UUID.randomUUID().toString().substring(0, 5)
    while (this.getRooms().find { it.roomCode == randomString } != null) {
        randomString = UUID.randomUUID().toString().substring(0, 5)
    }
    return randomString
}

suspend fun ServerState.startGame(roomCode: String) =
    this.getRooms().find { it.roomCode == roomCode }.toEither().flatMap {
        it.start(this)
        this.sendToRoom(roomCode, OutboundGameStarted.serializer(), OutboundGameStarted())
    }.mapToUnit()

fun GameState.getNextGameState(): GameState {
    val updatedBlocks = this.blocks.mapValues { pair ->
        val block = pair.value
        this.getNextGameStateAfterBlockMovement(pair.key, block)
    }

    val rowsCompleted = countCompletedRows(this.mapTiles)
    val updatedTiles = this.getTilesWithoutCompletedRows()

    return GameState(updatedTiles, this.players, updatedBlocks, this.score + rowsCompleted, this.currentTick + 1)
}

fun GameState.getNextGameStateAfterBlockMovement(playerId: Int, block: Block?): Block? {
    if (block == null) {
        return Block.getRandomBlock(playerId)
    }

    val connection = this.players[playerId] ?: throw Error("Missing connection for id $playerId")
    val blockAfterHorizontalMovement = block.handleHorizontalMovement(connection, this.mapTiles)
    val blockAfterRotationalMovement = blockAfterHorizontalMovement.handleRotationalMovement(connection)
    return blockAfterRotationalMovement.handleVerticalMovement(connection, playerId, this)
}

private fun Block.handleHorizontalMovement(connection: Connection, mapTiles: Tiles): Block =
    when {
        connection.pressingLeft && this.canMoveLeft(mapTiles) ->
            Block(this.x - 1, this.y, this.shape)
        connection.pressingRight && this.canMoveRight(mapTiles) ->
            Block(this.x + 1, this.y, this.shape)
        else -> this
    }

private fun Block.handleRotationalMovement(connection: Connection): Block =
    when {
        connection.pressingRotateLeft -> this.rotateAntiClockwise()
        connection.pressingRotateRight -> this.rotateClockwise()
        else -> this
    }

private fun Block.handleVerticalMovement(connection: Connection, player: Int, gameState: GameState): Block? =
    when {
        (connection.pressingDown || gameState.currentTick % 5 == 0) && this.canMoveDown(gameState.mapTiles) ->
            Block(this.x, this.y + 1, this.shape)
        !this.canMoveDown(gameState.mapTiles) -> {
            gameState.stampOntoTiles(player, this)
            null
        }
        else -> this
    }

private fun countCompletedRows(tiles: Tiles): Int =
    tiles.filter { row ->
        row.all { tile -> tile != 0 }
    }.size

private fun GameState.getTilesWithoutCompletedRows(): Tiles {
    val notCompletedRows = this.mapTiles.filter { row ->
        row.any { tile -> tile == 0 }
    }
    val rowsToAdd = mapTiles.size - notCompletedRows.size
    if(rowsToAdd <= 0) return this.mapTiles

    val emptyTilesToPrepend = Array(rowsToAdd) {
        Array(this.mapTiles.first().size) { 0 }
    }
    return emptyTilesToPrepend.plus(notCompletedRows)
}