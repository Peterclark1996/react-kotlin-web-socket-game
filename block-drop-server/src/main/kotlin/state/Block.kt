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

    fun isOverlappingTiles(mapTiles: Tiles): Boolean{
        this.shape.getSilhouette().forEachIndexed { rowIndex, row ->
            row.forEachIndexed { tileIndex, tile ->
                if(tile && mapTiles[this.y + rowIndex][this.x + tileIndex] != 0){
                    return true
                }
            }
        }
        return false
    }
}

fun rotateBlockClockwise(block: Block) = Block(block.x, block.y, block.shape.rotateClockwise())

fun rotateBlockAntiClockwise(block: Block) = Block(block.x, block.y, block.shape.rotateAntiClockwise())

fun translateBlockLeft(block: Block) = Block(block.x - 1, block.y, block.shape)

fun translateBlockRight(block: Block) = Block(block.x + 1, block.y, block.shape)

fun translateBlockDown(block: Block) = Block(block.x, block.y + 1, block.shape)

fun canTransformBlock(block: Block, mapTiles: Tiles, transformF: (Block) -> Block): Boolean {
    val transformedBlock = transformF(block)
    if(transformedBlock.x < 0){
        return false
    }
    if(transformedBlock.y < 0){
        return false
    }
    if(transformedBlock.x + transformedBlock.shape.getSilhouette().first().size > mapTiles.first().size){
        return false
    }
    if(transformedBlock.y + transformedBlock.shape.getSilhouette().size > mapTiles.size){
        return false
    }
    return !transformedBlock.isOverlappingTiles(mapTiles)
}