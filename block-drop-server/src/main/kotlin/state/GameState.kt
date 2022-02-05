package state

class GameState private constructor(
    private val tiles: Tiles,
    private val players: Map<Int, Connection>,
    private val blocks: Map<Int, Block?>
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

            return GameState(blankTiles, playersByIds, blocks)
        }
    }

    fun getTilesWithBlocks(): Tiles {
        val updatedTiles = Array(tiles.size) {
            IntArray(tiles.first().size) { 0 }
        }

        tiles.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { tileIndex, tile ->
                updatedTiles[rowIndex][tileIndex] = tile
            }
        }

        for (pair in blocks) {
            val block = pair.value ?: continue
            block.tiles.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { tileIndex, tile ->
                    updatedTiles[rowIndex + block.y][tileIndex + block.x] = tile
                }
            }
        }
        return updatedTiles
    }

    fun processToNextState(): GameState {
        val updatedBlocks = blocks.mapValues { pair ->
            val block = pair.value
            if (block != null) {
                if (block.canMoveDown(tiles)) {
                    Block(block.x, block.y + 1, block.tiles)
                } else {
                    block.tiles.forEachIndexed { rowIndex, row ->
                        row.forEachIndexed { tileIndex, tile ->
                            if(tile != 0){
                                tiles[rowIndex + block.y][tileIndex + block.x] = tile
                            }
                        }
                    }
                    null
                }
            } else Block(0, 0, Block.getRandomTilesForPlayerId(pair.key))
        }
        return GameState(tiles, players, updatedBlocks)
    }
}