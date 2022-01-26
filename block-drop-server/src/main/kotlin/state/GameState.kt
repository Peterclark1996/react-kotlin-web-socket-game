package state

class GameState private constructor(val tiles: Array<IntArray>, private val players: List<Connection>) {
    companion object {
        fun createNew(players: List<Connection>): GameState {
            val gridHeight = 20
            val gridWidth = 15
            val grid = Array(gridHeight) { i ->
                when (i) {
                    0, gridHeight - 1 -> IntArray(15) { -1 }
                    3 -> IntArray(15) { 1 }
                    6 -> IntArray(15) { 2 }
                    9 -> IntArray(15) { 3 }
                    else -> IntArray(gridWidth) {
                        if (i == 0 || i == gridWidth - 1) -1
                        else 0
                    }
                }
            }
            return GameState(grid, players)
        }
    }
}