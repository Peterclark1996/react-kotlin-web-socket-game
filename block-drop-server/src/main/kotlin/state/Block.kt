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
        fun getRandomBlock(): Block {
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
}

fun rotateBlockClockwise(block: Block) = Block(block.x, block.y, block.shape.rotateClockwise())

fun rotateBlockAntiClockwise(block: Block) = Block(block.x, block.y, block.shape.rotateAntiClockwise())

fun translateBlockLeft(block: Block) = Block(block.x - 1, block.y, block.shape)

fun translateBlockRight(block: Block) = Block(block.x + 1, block.y, block.shape)

fun translateBlockDown(block: Block) = Block(block.x, block.y + 1, block.shape)

fun canBlockMoveDown(block: Block, mapTiles: Tiles): Boolean {
    block.shape.getSilhouette().forEachIndexed { rowIndex, row ->
        if(block.y + rowIndex >= mapTiles.size - 1){
            return false
        }
        row.forEachIndexed { tileIndex, tile ->
            if(tile && mapTiles[block.y + rowIndex + 1][block.x + tileIndex] != 0){
                return false
            }
        }
    }
    return true
}

fun canBlockMoveLeft(block: Block, mapTiles: Tiles): Boolean {
    block.shape.getSilhouette().forEachIndexed { rowIndex, row ->
        if(block.x <= 0){
            return false
        }
        row.forEachIndexed { tileIndex, tile ->
            if(tile && mapTiles[block.y + rowIndex][block.x + tileIndex - 1] != 0){
                return false
            }
        }
    }
    return true
}

fun canBlockMoveRight(block: Block, mapTiles: Tiles): Boolean {
    block.shape.getSilhouette().forEachIndexed { rowIndex, row ->
        if(block.x + row.size >= mapTiles.first().size){
            return false
        }
        row.forEachIndexed { tileIndex, tile ->
            if(tile && mapTiles[block.y + rowIndex][block.x + tileIndex + 1] != 0){
                return false
            }
        }
    }
    return true
}

fun canBlockRotateLeft(block: Block, mapTiles: Tiles): Boolean {
    val rotatedSilhouette = rotateBlockAntiClockwise(block).shape.getSilhouette()
    if(block.x + rotatedSilhouette.first().size >= mapTiles.first().size){
        return false
    }
    if(block.y + rotatedSilhouette.size >= mapTiles.size){
        return false
    }
    rotatedSilhouette.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { tileIndex, tile ->
            if(tile && mapTiles[block.y + rowIndex][block.x + tileIndex] != 0){
                return false
            }
        }
    }
    return true
}

fun canBlockRotateRight(block: Block, mapTiles: Tiles): Boolean {
    val rotatedSilhouette = rotateBlockClockwise(block).shape.getSilhouette()
    if(block.x + rotatedSilhouette.first().size > mapTiles.first().size){
        return false
    }
    if(block.y + rotatedSilhouette.size > mapTiles.size){
        return false
    }
    rotatedSilhouette.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { tileIndex, tile ->
            if(tile && mapTiles[block.y + rowIndex][block.x + tileIndex] != 0){
                return false
            }
        }
    }
    return true
}