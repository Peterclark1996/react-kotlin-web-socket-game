package state

typealias Tiles = Array<Array<Int>>

typealias Silhouette = Array<Array<Boolean>>

class BlockShape private constructor(
    private val rotations: List<Silhouette>,
    private val rotationIndex: Int = rotations.indices.random()
) {
    companion object {
        fun createShapeOne() =
            BlockShape(listOf(shapeOne, shapeOneClockwise, shapeOneHalfRotation, shapeOneAntiClockwise))
        fun createShapeTwo() =
            BlockShape(listOf(shapeTwo, shapeTwoClockwise, shapeTwoHalfRotation, shapeTwoAntiClockwise))
        fun createShapeThree() =
            BlockShape(listOf(shapeThree, shapeThreeRotated))
        fun createShapeFour() =
            BlockShape(listOf(shapeFour))
        fun createShapeFive() =
            BlockShape(listOf(shapeFive, shapeFiveRotated))
        fun createShapeSix() =
            BlockShape(listOf(shapeSix, shapeSixRotated))
        fun createShapeSeven() =
            BlockShape(listOf(shapeSeven, shapeSevenClockwise, shapeSevenHalfRotated, shapeSevenAntiClockwise))
    }

    fun rotateClockwise() = BlockShape(rotations, (rotationIndex + 1) % rotations.size)
    fun rotateAntiClockwise() = BlockShape(rotations, (rotations.size + rotationIndex - 1) % rotations.size)

    fun getSilhouette() = rotations[rotationIndex]

    fun getTiles(player: Int) = rotations[rotationIndex].map { row ->
        row.map { tile ->
            if (tile) player
            else 0
        }
    }
}

val shapeOne = arrayOf(
    arrayOf(false, false, true),
    arrayOf(true, true, true)
)
val shapeOneClockwise = arrayOf(
    arrayOf(true, false),
    arrayOf(true, false),
    arrayOf(true, true)
)
val shapeOneHalfRotation = arrayOf(
    arrayOf(true, true, true),
    arrayOf(true, false, false)
)
val shapeOneAntiClockwise = arrayOf(
    arrayOf(true, true),
    arrayOf(false, true),
    arrayOf(false, true)
)

val shapeTwo = arrayOf(
    arrayOf(true, true, true),
    arrayOf(false, false, true)
)
val shapeTwoClockwise = arrayOf(
    arrayOf(false, true),
    arrayOf(false, true),
    arrayOf(true, true)
)
val shapeTwoHalfRotation = arrayOf(
    arrayOf(true, false, false),
    arrayOf(true, true, true)
)
val shapeTwoAntiClockwise = arrayOf(
    arrayOf(true, true),
    arrayOf(true, false),
    arrayOf(true, false)
)

val shapeThree = arrayOf(
    arrayOf(true, true, true, true)
)
val shapeThreeRotated = arrayOf(
    arrayOf(true),
    arrayOf(true),
    arrayOf(true),
    arrayOf(true)
)

val shapeFour = arrayOf(
    arrayOf(true, true),
    arrayOf(true, true)
)

val shapeFive = arrayOf(
    arrayOf(true, true, false),
    arrayOf(false, true, true)
)
val shapeFiveRotated = arrayOf(
    arrayOf(false, true),
    arrayOf(true, true),
    arrayOf(true, false)
)

val shapeSix = arrayOf(
    arrayOf(false, true, true),
    arrayOf(true, true, false)
)
val shapeSixRotated = arrayOf(
    arrayOf(true, false),
    arrayOf(true, true),
    arrayOf(false, true)
)

val shapeSeven = arrayOf(
    arrayOf(true, true, true),
    arrayOf(false, true, false)
)
val shapeSevenClockwise = arrayOf(
    arrayOf(false, true),
    arrayOf(true, true),
    arrayOf(false, true)
)
val shapeSevenHalfRotated = arrayOf(
    arrayOf(false, true, false),
    arrayOf(true, true, true)
)
val shapeSevenAntiClockwise = arrayOf(
    arrayOf(true, false),
    arrayOf(true, true),
    arrayOf(true, false)
)