package state

import arrow.core.Either
import toLeft
import toRight
import kotlin.math.ceil
import kotlin.math.floor

class GameState(
    val mapTiles: Tiles,
    val players: List<PlayerState>,
    val currentTick: Int,
    val spawnBlocks: List<Boolean>
) {
    companion object {
        fun createNew(connections: List<Connection>, gridHeight: Int, gridWidth: Int): Either<Error, GameState> {
            if (gridHeight < 5) return Error("Height is too small").toLeft()

            val centerSpawnBlocks =
                connections.map { Array(4) { false } }.reduce { acc, next -> acc.plus(true).plus(next) }

            if (centerSpawnBlocks.size > gridWidth) return Error("Width is too small").toLeft()

            val remainingWidth = (gridWidth - centerSpawnBlocks.size).toFloat()
            val spawnBlocks = Array(ceil(remainingWidth / 2).toInt()) { true }
                .plus(centerSpawnBlocks)
                .plus(Array(floor(remainingWidth / 2).toInt()) { true })

            val blankTiles = Array(gridHeight) { row ->
                Array(gridWidth) { cell ->
                    if (row < 4 && spawnBlocks[cell]) {
                        -1
                    } else {
                        0
                    }
                }
            }

            val playerList = connections.mapIndexed { i, connection ->
                PlayerState(
                    i + 1,
                    connection,
                    null,
                    0,
                    blockHorizontalMovementThisTick = false,
                    isDead = false
                )
            }

            return GameState(
                blankTiles,
                playerList,
                0,
                spawnBlocks.toList()
            ).toRight()
        }
    }

    fun getTilesWithBlocks(): Tiles {
        val updatedTiles = Array(mapTiles.size) {
            Array(mapTiles.first().size) { 0 }
        }

        mapTiles.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { tileIndex, tile ->
                updatedTiles[rowIndex][tileIndex] = tile
            }
        }

        players.forEach {
            val block = it.block
            block?.shape?.getTiles(it.id)?.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { tileIndex, tile ->
                    if (tile != 0) {
                        updatedTiles[rowIndex + block.y][tileIndex + block.x] = tile
                    }
                }
            }
        }
        return updatedTiles
    }

    fun stampOntoTiles(player: Int, block: Block) =
        block.shape.getTiles(player).forEachIndexed { rowIndex, row ->
            row.forEachIndexed { tileIndex, tile ->
                if (tile != 0) {
                    mapTiles[rowIndex + block.y][tileIndex + block.x] = tile
                }
            }
        }
}

fun updateBlockPositionForConnection(
    gameState: GameState,
    connection: Connection,
    disableHorizontalMovement: Boolean,
    moveF: (Block) -> Block
): GameState {
    val player = gameState.players.find { it.connection == connection } ?: return gameState
    val block = player.block ?: return gameState
    if (canTransformBlock(block, gameState.mapTiles, moveF)) {
        val updatedPlayer = PlayerState(
            player.id,
            player.connection,
            moveF(block),
            player.score,
            player.blockHorizontalMovementThisTick || disableHorizontalMovement,
            player.isDead
        )
        val updatedPlayerList = gameState.players.filter { it.id != updatedPlayer.id }.plus(updatedPlayer)
        return GameState(gameState.mapTiles, updatedPlayerList, gameState.currentTick, gameState.spawnBlocks)
    }
    return gameState
}

fun GameState.getNextGameState(): GameState {
    val playerWithUpdatedBlocks = this.players.map { player ->
        this.getNextPlayerStateAfterBlockMovement(player)
    }

    val rowsCompleted = countCompletedRows(this.mapTiles)
    val updatedTiles = this.getTilesWithoutCompletedRows()

    val playersWithUpdatedScore = playerWithUpdatedBlocks.map {
        PlayerState(
            it.id,
            it.connection,
            it.block,
            it.score + (rowsCompleted * 100),
            it.blockHorizontalMovementThisTick,
            it.isDead
        )
    }

    return GameState(updatedTiles, playersWithUpdatedScore, this.currentTick + 1, spawnBlocks)
}

fun GameState.getNextPlayerStateAfterBlockMovement(player: PlayerState): PlayerState {
    if (player.block == null) {
        val newBlock =
            if (player.isDead) {
                null
            } else {
                val newRandomShape = BlockShape.getRandomBlockShape()
                val playerSpawnOffset =
                    ((this.mapTiles.first().size - ((this.players.count() * 5) - 1)) / 2) + ((player.id - 1) * 5)
                val blockOffset = (4 - newRandomShape.getSilhouette().first().size) / 2
                Block(
                    playerSpawnOffset + blockOffset,
                    0,
                    newRandomShape
                )
            }
        val hasThePlayerJustDied = newBlock?.isOverlappingTiles(this.mapTiles) ?: true
        return PlayerState(
            player.id,
            player.connection,
            if (hasThePlayerJustDied) null else newBlock,
            player.score,
            player.blockHorizontalMovementThisTick,
            hasThePlayerJustDied
        )
    }

    val blockAfterAllMovement = player.block
        .handleHorizontalMovement(player, this.mapTiles)
        .handleVerticalMovement(player, this)
    return PlayerState(
        player.id,
        player.connection,
        blockAfterAllMovement,
        player.score,
        false,
        player.isDead
    )
}

private fun Block.handleHorizontalMovement(player: PlayerState, mapTiles: Tiles): Block =
    when {
        !player.blockHorizontalMovementThisTick &&
                player.connection.pressingLeft &&
                canTransformBlock(this, mapTiles, ::translateBlockLeft) ->
            translateBlockLeft(this)
        !player.blockHorizontalMovementThisTick &&
                player.connection.pressingRight &&
                canTransformBlock(this, mapTiles, ::translateBlockRight) ->
            translateBlockRight(this)
        else -> this
    }

private fun Block.handleVerticalMovement(player: PlayerState, gameState: GameState): Block? =
    when {
        (player.connection.pressingDown || gameState.currentTick % 5 == 0) &&
                canTransformBlock(this, gameState.mapTiles, ::translateBlockDown) ->
            translateBlockDown(this)
        !canTransformBlock(this, gameState.mapTiles, ::translateBlockDown) -> {
            gameState.stampOntoTiles(player.id, this)
            null
        }
        else -> this
    }

private fun countCompletedRows(tiles: Tiles): Int =
    tiles.filter { row ->
        row.all { tile -> tile != 0 }
    }.size

private fun GameState.getTilesWithoutCompletedRows(): Tiles {
    val nonCompletedRows = this.mapTiles.filter { row ->
        row.any { tile -> tile == 0 }
    }
    val rowsToAdd = mapTiles.size - nonCompletedRows.size
    if (rowsToAdd <= 0) return this.mapTiles

    val emptyTilesToPrepend = Array(rowsToAdd) {
        Array(this.mapTiles.first().size) { 0 }
    }

    val tilesWithoutSpawnBlocks = emptyTilesToPrepend.plus(nonCompletedRows).map { row ->
        row.map { cell ->
            if(cell == -1) 0
            else cell
        }
    }

    val tilesWithSpawnBlocks = tilesWithoutSpawnBlocks.mapIndexed { rowIndex, row ->
        if(rowIndex > 3){
            row.toTypedArray()
        }else{
            row.mapIndexed { cellIndex, cell ->
                if(this.spawnBlocks[cellIndex]) -1
                else cell
            }.toTypedArray()
        }
    }.toTypedArray()

    return tilesWithSpawnBlocks
}