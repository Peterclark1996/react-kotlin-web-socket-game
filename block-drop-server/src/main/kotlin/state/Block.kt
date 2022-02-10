package state

import state.BlockShape.Companion.createShapeFive
import state.BlockShape.Companion.createShapeFour
import state.BlockShape.Companion.createShapeOne
import state.BlockShape.Companion.createShapeSeven
import state.BlockShape.Companion.createShapeSix
import state.BlockShape.Companion.createShapeThree
import state.BlockShape.Companion.createShapeTwo

data class Block(val x: Int, val y: Int, val shape: BlockShape) {
    companion object {
        fun getRandomBlock(player: Int): Block {
            val possibleShapes = listOf(
                ::createShapeOne,
                ::createShapeTwo,
                ::createShapeThree,
                ::createShapeFour,
                ::createShapeFive,
                ::createShapeSix,
                ::createShapeSeven
            )
            val selectedShape = possibleShapes.random().invoke()
            return Block(0, 0, selectedShape)
        }
    }

    fun rotateClockwise() = Block(x, y, shape.rotateClockwise())

    fun rotateAntiClockwise() = Block(x, y, shape.rotateAntiClockwise())

    fun canMoveDown(mapTiles: Tiles): Boolean {
        shape.getSilhouette().forEachIndexed { rowIndex, row ->
            if(y + rowIndex >= mapTiles.size - 1){
                return false
            }
            row.forEachIndexed { tileIndex, tile ->
                if(tile && mapTiles[y + rowIndex + 1][x + tileIndex] != 0){
                    return false
                }
            }
        }
        return true
    }

    fun canMoveLeft(mapTiles: Tiles): Boolean {
        shape.getSilhouette().forEachIndexed { rowIndex, row ->
            if(x <= 0){
                return false
            }
            row.forEachIndexed { tileIndex, tile ->
                if(tile && mapTiles[y + rowIndex][x + tileIndex - 1] != 0){
                    return false
                }
            }
        }
        return true
    }

    fun canMoveRight(mapTiles: Tiles): Boolean {
        shape.getSilhouette().forEachIndexed { rowIndex, row ->
            if(x + row.size >= mapTiles.first().size){
                return false
            }
            row.forEachIndexed { tileIndex, tile ->
                if(tile && mapTiles[y + rowIndex][x + tileIndex + 1] != 0){
                    return false
                }
            }
        }
        return true
    }
}