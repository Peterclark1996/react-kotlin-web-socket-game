package state

data class Block(val x: Int, val y: Int, val shape: BlockShape) {
    fun isOverlappingTiles(mapTiles: Tiles): Boolean {
        this.shape.getSilhouette().forEachIndexed { rowIndex, row ->
            row.forEachIndexed { tileIndex, tile ->
                if (tile && mapTiles[this.y + rowIndex][this.x + tileIndex] != 0) {
                    return true
                }
            }
        }
        return false
    }

    fun isOverlappingOtherPlayerBlocks(gameState: GameState, playerId: Int): Boolean {
        gameState.players.filter { it.id != playerId }.forEach {
            val playerBlock = it.block
            if (playerBlock != null) {
                this.shape.getSilhouette().forEachIndexed { rowIndex, row ->
                    row.forEachIndexed { tileIndex, tile ->
                        val y = this.y - playerBlock.y + rowIndex
                        val x = this.x - playerBlock.x + tileIndex
                        if (y >= 0 && y < playerBlock.shape.getSilhouette().size) {
                            if (x >= 0 && x < playerBlock.shape.getSilhouette().first().size) {
                                if (tile && playerBlock.shape.getSilhouette()[y][x]) {
                                    return true
                                }
                            }
                        }
                    }
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

fun canTransformBlock(
    block: Block,
    gameState: GameState,
    playerId: Int,
    isBlockedByPlayers: Boolean,
    transformF: (Block) -> Block
): Boolean {
    val transformedBlock = transformF(block)
    if (transformedBlock.x < 0) {
        return false
    }
    if (transformedBlock.y < 0) {
        return false
    }
    if (transformedBlock.x + transformedBlock.shape.getSilhouette().first().size > gameState.mapTiles.first().size) {
        return false
    }
    if (transformedBlock.y + transformedBlock.shape.getSilhouette().size > gameState.mapTiles.size) {
        return false
    }
    return !transformedBlock.isOverlappingTiles(gameState.mapTiles) &&
            (!isBlockedByPlayers || !transformedBlock.isOverlappingOtherPlayerBlocks(gameState, playerId))
}