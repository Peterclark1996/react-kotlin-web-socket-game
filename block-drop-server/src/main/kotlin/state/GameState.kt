package state

class GameState (
    val mapTiles: Tiles,
    val players: Map<Int, Connection>,
    val blocks: Map<Int, Block?>,
    val score: Int,
    val currentTick: Int
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

            return GameState(blankTiles, playersByIds, blocks, 0, 0)
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

    fun stampOntoTiles(player: Int, block: Block) =
        block.shape.getTiles(player).forEachIndexed { rowIndex, row ->
            row.forEachIndexed { tileIndex, tile ->
                if (tile != 0) {
                    mapTiles[rowIndex + block.y][tileIndex + block.x] = tile
                }
            }
        }
}