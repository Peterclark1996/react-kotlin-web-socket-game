package state

data class Block(val x: Int, val y: Int, val tiles: Tiles) {
    companion object {
        fun getRandomTilesForPlayerId(player: Int): Tiles {
            val possibleBlocks = listOf(shapeOne, shapeTwo, shapeThree, shapeFour, shapeFive, shapeSix, shapeSeven)
            val selectedBlock = possibleBlocks.random()
            return selectedBlock.map { row ->
                row.map {
                    if (it) player
                    else 0
                }.toIntArray()
            }.toTypedArray()
        }
    }

    fun canMoveDown(mapTiles: Tiles): Boolean {
        tiles.forEachIndexed { rowIndex, row ->
            if(y + rowIndex >= mapTiles.size - 1){
                return false
            }
            row.forEachIndexed { tileIndex, tile ->
                if(tile != 0 && mapTiles[y + rowIndex + 1][x + tileIndex] != 0){
                    return false
                }
            }
        }
        return true
    }

    fun canMoveLeft(mapTiles: Tiles): Boolean {
        tiles.forEachIndexed { rowIndex, row ->
            if(x <= 0){
                return false
            }
            row.forEachIndexed { tileIndex, tile ->
                if(tile != 0 && mapTiles[y + rowIndex][x + tileIndex - 1] != 0){
                    return false
                }
            }
        }
        return true
    }

    fun canMoveRight(mapTiles: Tiles): Boolean {
        tiles.forEachIndexed { rowIndex, row ->
            if(x + row.size >= mapTiles.first().size){
                return false
            }
            row.forEachIndexed { tileIndex, tile ->
                if(tile != 0 && mapTiles[y + rowIndex][x + tileIndex + 1] != 0){
                    return false
                }
            }
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Block

        if (x != other.x) return false
        if (y != other.y) return false
        if (!tiles.contentDeepEquals(other.tiles)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + tiles.contentDeepHashCode()
        return result
    }
}