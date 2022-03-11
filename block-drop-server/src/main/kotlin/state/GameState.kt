package state

class GameState(
    val mapTiles: Tiles,
    val players: List<PlayerState>,
    val currentTick: Int,
    val spawnBlocks: List<Boolean>
) {
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