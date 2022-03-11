package state

class PlayerState(
    val id: Int,
    val connection: Connection,
    val block: Block?,
    val score: Int,
    val spawnOffset: Int,
    val blockHorizontalMovementThisTick: Boolean,
    val isDead: Boolean
)