package state

class GameState(
    val mapTiles: Tiles,
    val players: List<PlayerState>,
    val currentTick: Int
) {
    companion object {
        fun createNew(connections: List<Connection>): GameState {
            val gridHeight = 10
            val gridWidth = 10
            val blankTiles = Array(gridHeight) {
                Array(gridWidth) { 0 }
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

            return GameState(blankTiles, playerList, 0)
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
        return GameState(gameState.mapTiles, updatedPlayerList, gameState.currentTick)
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

    return GameState(updatedTiles, playersWithUpdatedScore, this.currentTick + 1)
}

fun GameState.getNextPlayerStateAfterBlockMovement(player: PlayerState): PlayerState {
    if (player.block == null) {
        val newBlock =
            if (player.isDead) { null }
            else { Block.getRandomBlock() }
        val hasThePlayerJustDied = newBlock?.isOverlappingTiles(this.mapTiles) ?: true
        return PlayerState(
            player.id,
            player.connection,
            if(hasThePlayerJustDied) null else newBlock,
            player.score,
            player.blockHorizontalMovementThisTick,
            hasThePlayerJustDied
        )
    }

    val blockAfterAllMovement = player.block
        .handleHorizontalMovement(player, this.mapTiles)
        .handleVerticalMovement(player, this)
    return PlayerState(player.id, player.connection, blockAfterAllMovement, player.score, false, player.isDead)
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
    val notCompletedRows = this.mapTiles.filter { row ->
        row.any { tile -> tile == 0 }
    }
    val rowsToAdd = mapTiles.size - notCompletedRows.size
    if (rowsToAdd <= 0) return this.mapTiles

    val emptyTilesToPrepend = Array(rowsToAdd) {
        Array(this.mapTiles.first().size) { 0 }
    }
    return emptyTilesToPrepend.plus(notCompletedRows)
}