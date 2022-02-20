package state

class Player(
    val id: Int,
    val connection: Connection,
    val block: Block?,
    val blockHorizontalMovementThisTick: Boolean = false
)

class GameState(
    val mapTiles: Tiles,
    val players: List<Player>,
    val score: Int,
    val currentTick: Int
) {
    companion object {
        fun createNew(connections: List<Connection>): GameState {
            val gridHeight = 10
            val gridWidth = 10
            val blankTiles = Array(gridHeight) {
                Array(gridWidth) { 0 }
            }

            val playerList = connections.mapIndexed { i, connection -> Player(i + 1, connection, null) }

            return GameState(blankTiles, playerList, 0, 0)
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
    canMoveF: (Block, Tiles) -> Boolean,
    moveF: (Block) -> Block
): GameState {
    val player = gameState.players.find { it.connection == connection } ?: return gameState
    val block = player.block ?: return gameState
    if (canMoveF(block, gameState.mapTiles)) {
        val updatedPlayer = Player(
            player.id,
            player.connection,
            moveF(block),
            player.blockHorizontalMovementThisTick || disableHorizontalMovement
        )
        val updatedPlayerList = gameState.players.filter { it.id != updatedPlayer.id }.plus(updatedPlayer)
        return GameState(gameState.mapTiles, updatedPlayerList, gameState.score, gameState.currentTick)
    }
    return gameState
}

fun GameState.getNextGameState(): GameState {
    val updatedPlayers = this.players.map { player ->
        this.getNextPlayerStateAfterBlockMovement(player)
    }

    val rowsCompleted = countCompletedRows(this.mapTiles)
    val updatedTiles = this.getTilesWithoutCompletedRows()

    return GameState(updatedTiles, updatedPlayers, this.score + rowsCompleted, this.currentTick + 1)
}

fun GameState.getNextPlayerStateAfterBlockMovement(player: Player): Player {
    if (player.block == null) {
        return Player(player.id, player.connection, Block.getRandomBlock())
    }

    val blockAfterAllMovement = player.block
        .handleHorizontalMovement(player, this.mapTiles)
        .handleVerticalMovement(player, this)
    return Player(player.id, player.connection, blockAfterAllMovement, false)
}

private fun Block.handleHorizontalMovement(player: Player, mapTiles: Tiles): Block =
    when {
        !player.blockHorizontalMovementThisTick &&
        player.connection.pressingLeft &&
        canBlockMoveLeft(this, mapTiles) ->
            translateBlockLeft(this)
        !player.blockHorizontalMovementThisTick &&
        player.connection.pressingRight &&
        canBlockMoveRight(this, mapTiles) ->
            translateBlockRight(this)
        else -> this
    }

private fun Block.handleVerticalMovement(player: Player, gameState: GameState): Block? =
    when {
        (player.connection.pressingDown || gameState.currentTick % 5 == 0) &&
        canBlockMoveDown(this, gameState.mapTiles) ->
            translateBlockDown(this)
        !canBlockMoveDown(this, gameState.mapTiles) -> {
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