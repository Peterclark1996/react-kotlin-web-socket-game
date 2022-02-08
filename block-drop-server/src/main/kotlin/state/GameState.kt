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
                IntArray(gridWidth) { 0 }
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
            IntArray(mapTiles.first().size) { 0 }
        }

        mapTiles.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { tileIndex, tile ->
                updatedTiles[rowIndex][tileIndex] = tile
            }
        }

        for (pair in blocks) {
            val block = pair.value ?: continue
            block.tiles.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { tileIndex, tile ->
                    if(tile != 0){
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
        if (this == null){
            return Block(0, 0, Block.getRandomTilesForPlayerId(playerId))
        }

        val playerMovedBlock = this.handlePlayerMovement(playerId)

        if (tick % 5 != 0){
            return playerMovedBlock
        }

        if (playerMovedBlock.canMoveDown(mapTiles)) {
            return Block(playerMovedBlock.x, playerMovedBlock.y + 1, playerMovedBlock.tiles)
        }

        playerMovedBlock.stampOntoTiles()
        return null
    }

    private fun Block.handlePlayerMovement(playerId: Int): Block {
        val connection = players[playerId] ?: throw Error("Missing connection for id $playerId")
        return when {
            connection.pressedKey == KeyTypes.DOWN && this.canMoveDown(mapTiles) ->
                Block(this.x, this.y + 1, this.tiles)
            connection.pressedKey == KeyTypes.LEFT && this.canMoveLeft(mapTiles) ->
                Block(this.x - 1, this.y, this.tiles)
            connection.pressedKey == KeyTypes.RIGHT && this.canMoveRight(mapTiles) ->
                Block(this.x + 1, this.y, this.tiles)
            else -> this
        }
    }

    private fun Block.stampOntoTiles() =
        this.tiles.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { tileIndex, tile ->
                if (tile != 0) {
                    mapTiles[rowIndex + this.y][tileIndex + this.x] = tile
                }
            }
        }
}