package day09

import readInput
import kotlin.math.absoluteValue
import kotlin.reflect.KMutableProperty

enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    companion object {
        fun fromChar(char: Char): Direction = when (char) {
            'U' -> UP
            'D' -> DOWN
            'L' -> LEFT
            'R' -> RIGHT
            else -> throw IllegalArgumentException("Invalid direction: $char")
        }
    }
}

interface Point {
    val x: Int
    val y: Int

    operator fun component1(): Int = x
    operator fun component2(): Int = y
}

interface Child {
    val child: Child?
}

enum class RelativePosition(val v: Int) {
    NEGATIVE(-1), ZERO(0), POSITIVE(1);

    infix fun isOpposite(other: RelativePosition): Boolean = this.v == -other.v
    operator fun unaryMinus(): RelativePosition = when (this) {
        NEGATIVE -> POSITIVE
        ZERO -> ZERO
        POSITIVE -> NEGATIVE
    }

    companion object {
        operator fun invoke(x: Int): RelativePosition = when (x) {
            -1 -> NEGATIVE
            0 -> ZERO
            1 -> POSITIVE
            else -> throw IllegalArgumentException("Invalid relative position: $x")
        }
    }
}

data class Relative(val x: RelativePosition, val y: RelativePosition) {
    fun isOrthogonal() = x == RelativePosition.ZERO || y == RelativePosition.ZERO
    fun isDiagonal() = x != RelativePosition.ZERO && y != RelativePosition.ZERO
    fun isNeutral() = x == RelativePosition.ZERO && y == RelativePosition.ZERO
    infix fun isInverse(other: Relative): Boolean = this.x isOpposite other.x && this.y isOpposite other.y
    operator fun unaryMinus(): Relative = Relative(-x, -y)
    fun isSignificant(other: Relative): Boolean {
        if (this.isNeutral() || other.isNeutral()) return false
        if ((this.x.v + other.x.v).absoluteValue == 2) return true
        if ((this.y.v + other.y.v).absoluteValue == 2) return true
        return false
    }

    companion object {
        operator fun invoke(x: Int, y: Int): Relative = Relative(RelativePosition(x), RelativePosition(y))
    }
}

interface Var<T> {
    var value: T
}

fun <T> apply(value: KMutableProperty<T>, transformation: Var<T>.() -> Unit) {
    object: Var<T> {
        override var value: T
            set(v) {
                value.setter.call(v)
            }
            get() = value.getter.call()
    }.transformation()
}

fun main() {

    fun parseMoves(input: List<String>): List<Direction> {
        val regex = Regex("""([UDLR]) (\d+)""")
        return buildList {
            input.forEach {
                val (direction, distance) = regex.matchEntire(it)!!.destructured
                repeat(distance.toInt()) { add(Direction.fromChar(direction[0])) }
            }
        }
    }

    data class SimplePoint(override val x: Int, override val y: Int): Point

    fun Point.toSimplePoint(): SimplePoint = SimplePoint(x, y)

    data class Tail(override var x: Int, override var y: Int, val id: Int, override var child: Tail? = null): Point,
        Child {
        constructor(id: Int, child: Tail? = null): this(0, 0, id, child)

        val visited: MutableSet<SimplePoint> = mutableSetOf(this.toSimplePoint())
        fun parentMove(fromX: Int, fromY: Int, toX: Int, toY: Int, debug: Boolean = false) {
            val (oldX, oldY) = this
            val relativeToMe = Relative(fromX - oldX, fromY - oldY)
            val moveDirection = Relative(toX - fromX, toY - fromY)
            if (!moveDirection.isSignificant(relativeToMe)) {
                if (debug) println("Tail $id: Neutral move, early exit"); return
            } // Diagonal moveRelative is only possible when there is more than one chain
            if (moveDirection.isOrthogonal()) {
                this.x = fromX
                this.y = fromY
            } else {
                if (relativeToMe.x.v + moveDirection.x.v == 0) {
                    this.y += moveDirection.y.v
                } else if (relativeToMe.y.v + moveDirection.y.v == 0) {
                    this.x += moveDirection.x.v
                } else {
                    this.x += moveDirection.x.v
                    this.y += moveDirection.y.v
                }
            }
            visited.add(this.toSimplePoint())
            if (debug) println("from=($oldX,$oldY) to=($x,$y) $this")
            child?.parentMove(oldX, oldY, x, y, debug)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Tail

            if (x != other.x) return false
            if (y != other.y) return false

            return true
        }

        override fun hashCode(): Int {
            var result = x
            result = 31 * result + y
            return result
        }
    }

    data class Head(override var x: Int, override var y: Int, val child: Tail): Point {
        fun move(direction: Direction, debug: Boolean = false) {
            val (oldX, oldY) = this
            when (direction) {
                Direction.UP -> y++
                Direction.DOWN -> y--
                Direction.LEFT -> x--
                Direction.RIGHT -> x++
            }
            if (debug) println("Moved from ($oldX, $oldY) to ($x, $y)")
            child.parentMove(oldX, oldY, x, y, debug)
        }
    }

    data class Visualize(val boardSize: Int, val xOff: Int = 0, val yOff: Int = 0)

    fun getCharacter(renderX: Int, renderY: Int, head: Head): Char {
        var tail: Tail? = null
        val point = SimplePoint(renderX, renderY)
        val childSequence = sequence {
            var current: Tail? = head.child
            while (current != null) {
                yield(current)
                current = current.child
            }
        }

        if (renderX == 0 && renderY == 0) return 's'
        if (head.toSimplePoint() == point) return 'H'
        if (childSequence.firstOrNull { it.toSimplePoint() == point }?.let { point -> tail = point; true } == true) return tail!!.id.toString()[0]
        if (point in childSequence.last().visited) return '#'
        return '.'
    }

    fun List<Direction>.navigate(head: Head, visualize: Visualize? = null) {
        var lastDirection: Direction? = null
        forEach {
            if (visualize != null/* && lastDirection != null && it != lastDirection*/) {
                repeat(visualize.boardSize) { y ->
                    val renderY = visualize.boardSize - 1 - y - visualize.yOff
                    print(renderY.toString().padStart(2, ' ')+"  ")
                    repeat(visualize.boardSize) { x ->
                        val renderX = x - visualize.xOff
                        print(getCharacter(renderX, renderY, head)+"  ")
                    }
                    println()
                }
                print(" ".repeat(2))
                repeat(visualize.boardSize) { print((it - visualize.xOff).toString().padStart(3, ' ')) }
                println()
                print("--".repeat(visualize.boardSize))
                println()
            }
            head.move(it, visualize != null)
            if (visualize != null) println(it)
            lastDirection = it
        }
    }

    fun part1(input: List<String>, visualize: Visualize? = null): Int {
        val moves = parseMoves(input)
        val head = Head(0, 0, Tail(1))
        moves.navigate(head, visualize)
        return head.child.visited.size
    }

    fun part2(input: List<String>, visualize: Visualize? = null): Int {
        val last = Tail(9)
        var current = last
        repeat(8) {
            current = Tail(8 - it, current)
        }

        val head = Head(0, 0, current)
        parseMoves(input).navigate(head, visualize)
        return last.visited.size
    }

    val testInput = readInput(9, true)
    part1(testInput, Visualize(6)).let { check(it == 13) { println(it) } }
    part2(testInput/*, Visualize(6)*/).let { check(it == 1) { println(it) } }
    // part2(readInput("Day09_test_2")/*, Visualize(26, xOff = 11, yOff = 5)*/).let { check(it == 36) { println(it) } }

    val input = readInput(9)
    println(part1(input))
    println(part2(input))
}