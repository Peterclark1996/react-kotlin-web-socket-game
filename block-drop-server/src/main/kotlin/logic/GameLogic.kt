package logic

import arrow.core.Either
import state.*
import toLeft
import toRight
import kotlin.math.ceil
import kotlin.math.floor

fun createNewGameState(connections: List<Connection>, gridHeight: Int, gridWidth: Int): Either<Error, GameState> {
    if (gridHeight < 5) return Error("Height is too small").toLeft()
    if (gridHeight > 26) return Error("Height is too large").toLeft()
    if (gridWidth > 32) return Error("Width is too large").toLeft()

    val centerSpawnBlocks =
        connections.map { Array(4) { false } }.reduce { acc, next -> acc.plus(true).plus(next) }

    if (centerSpawnBlocks.size > gridWidth) return Error("Width is too small").toLeft()

    val remainingWidth = (gridWidth - centerSpawnBlocks.size).toFloat()
    val remainingWidthLeft = ceil(remainingWidth / 2).toInt()
    val remainingWidthRight = floor(remainingWidth / 2).toInt()

    val spawnBlocks = Array(remainingWidthLeft) { true }
        .plus(centerSpawnBlocks)
        .plus(Array(remainingWidthRight) { true })

    val blankTiles = Array(gridHeight) { row ->
        Array(gridWidth) { cell ->
            if (row < 4 && spawnBlocks[cell]) {
                -1
            } else {
                0
            }
        }
    }

    val playerList = connections.mapIndexed { index, connection ->
        PlayerState(
            index + 1,
            connection,
            null,
            0,
            remainingWidthLeft + (index * 5),
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

fun updateBlockPositionForConnection(
    gameState: GameState,
    connection: Connection,
    disableHorizontalMovement: Boolean,
    moveF: (Block) -> Block
): GameState {
    val player = gameState.players.find { it.connection == connection } ?: return gameState
    val block = player.block ?: return gameState
    if (canTransformBlock(block, gameState, player.id, true, moveF)) {
        val updatedPlayer = PlayerState(
            player.id,
            player.connection,
            moveF(block),
            player.score,
            player.spawnOffset,
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
            it.spawnOffset,
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
                val blockOffset = (4 - newRandomShape.getSilhouette().first().size) / 2
                Block(
                    player.spawnOffset + blockOffset,
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
            player.spawnOffset,
            player.blockHorizontalMovementThisTick,
            hasThePlayerJustDied
        )
    }

    val blockAfterAllMovement = player.block
        .handleHorizontalMovement(player, this)
        .handleVerticalMovement(player, this)
    return PlayerState(
        player.id,
        player.connection,
        blockAfterAllMovement,
        player.score,
        player.spawnOffset,
        false,
        player.isDead
    )
}

private fun Block.handleHorizontalMovement(player: PlayerState, gameState: GameState): Block =
    when {
        !player.blockHorizontalMovementThisTick &&
                player.connection.pressingLeft &&
                canTransformBlock(this, gameState, player.id, true, ::translateBlockLeft) ->
            translateBlockLeft(this)
        !player.blockHorizontalMovementThisTick &&
                player.connection.pressingRight &&
                canTransformBlock(this, gameState, player.id, true, ::translateBlockRight) ->
            translateBlockRight(this)
        else -> this
    }

private fun Block.handleVerticalMovement(player: PlayerState, gameState: GameState): Block? =
    when {
        (player.connection.pressingDown || gameState.currentTick % gameState.getTicksBetweenGravity() == 0) &&
                canTransformBlock(this, gameState, player.id, true, ::translateBlockDown) ->
            translateBlockDown(this)
        !canTransformBlock(this, gameState, player.id,false, ::translateBlockDown) -> {
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

private fun GameState.getTicksBetweenGravity() =
    when{
        this.currentTick < 100 -> 5
        this.currentTick < 500 -> 4
        this.currentTick < 1000 -> 3
        this.currentTick < 3000 -> 2
        else -> 1
    }