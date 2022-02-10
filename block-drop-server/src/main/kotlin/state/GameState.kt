package state

class GameState private constructor(
    private val mapTiles: Tiles,
    private val players: Map<Int, Connection>,
    private val blocks: Map<Int, Block?>,
    private var tick: Int
) {
    companion object {
        fun createNew(players: List<Connection>): GameState {
            val gridHeight = 10
            val gridWidth = 10
            val blankTiles = Array(gridHeight) {
                Array(gridWidth) { 0 }
            }

            val playersByIds = players.mapIndexed { i, player ->
                i + 1 to player
            }.toMap()

            val blocks = playersByIds.map {
                it.key to null
            }.toMap()

            return GameState(blankTiles, playersByIds, blocks, 0)
        }
    }

    fun getTick() = tick

    fun getTilesWithBlocks(): Tiles {
        val updatedTiles = Array(mapTiles.size) {
            Array(mapTiles.first().size) { 0 }
        }

        mapTiles.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { tileIndex, tile ->
                updatedTiles[rowIndex][tileIndex] = tile
            }
        }

        for (pair in blocks) {
            val block = pair.value ?: continue
            block.shape.getTiles(pair.key).forEachIndexed { rowIndex, row ->
                row.forEachIndexed { tileIndex, tile ->
                    if (tile != 0) {
                        updatedTiles[rowIndex + block.y][tileIndex + block.x] = tile
                    }
                }
            }
        }
        return updatedTiles
    }

    fun getNextState(): GameState {
        val updatedBlocks = blocks.mapValues { pair ->
            val block = pair.value
            block.getNextState(pair.key)
        }
        return GameState(mapTiles, players, updatedBlocks, tick + 1)
    }

    private fun Block?.getNextState(playerId: Int): Block? {
        if (this == null) {
            return Block.getRandomBlock(playerId)
        }

        val connection = players[playerId] ?: throw Error("Missing connection for id $playerId")
        val blockAfterHorizontalMovement = this.handleHorizontalMovement(connection)
        val blockAfterRotationalMovement = blockAfterHorizontalMovement.handleRotationalMovement(connection)
        return blockAfterRotationalMovement.handleVerticalMovement(connection, tick, playerId)
    }

    private fun Block.handleHorizontalMovement(connection: Connection): Block =
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

    private fun Block.handleVerticalMovement(connection: Connection, tick: Int, player: Int): Block? =
        when {
            (connection.pressingDown || tick % 5 == 0) && this.canMoveDown(mapTiles) ->
                Block(this.x, this.y + 1, this.shape)
            !this.canMoveDown(mapTiles) -> {
                this.stampOntoTiles(player)
                null
            }
            else -> this
        }

    private fun Block.stampOntoTiles(player: Int) =
        this.shape.getTiles(player).forEachIndexed { rowIndex, row ->
            row.forEachIndexed { tileIndex, tile ->
                if (tile != 0) {
                    mapTiles[rowIndex + this.y][tileIndex + this.x] = tile
                }
            }
        }
}