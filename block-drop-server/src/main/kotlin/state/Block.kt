package state

data class Block(val x: Int, val y: Int, val tiles: Tiles) {
    companion object {
        private val shapeOne = arrayOf(
            arrayOf(false, false, true),
            arrayOf(true, true, true)
        )
        private val shapeTwo = arrayOf(
            arrayOf(true, true, true),
            arrayOf(false, false, true)
        )
        private val shapeThree = arrayOf(
            arrayOf(true, true, true, true)
        )
        private val shapeFour = arrayOf(
            arrayOf(false, true, true),
            arrayOf(false, true, true)
        )
        private val shapeFive = arrayOf(
            arrayOf(true, true, false),
            arrayOf(false, true, true)
        )
        private val shapeSix = arrayOf(
            arrayOf(false, true, true),
            arrayOf(true, true, false)
        )
        private val shapeSeven = arrayOf(
            arrayOf(true, true, true),
            arrayOf(false, true, false)
        )

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