package day12

import day12.Node.Companion.invoke
import day08.xyv
import readInput

enum class MoveDirection {
    UP, DOWN, LEFT, RIGHT
}

open class Height(val value: Int) {
    fun canMoveTo(height: Height): Boolean {
        return this.value <= (height.value + 1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Height

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value
    }
}

object StartHeight: Height(0)
object EndHeight: Height(25)

val letterToHeight = buildMap {
    iterator {
        yieldAll('a'..'z')
    }.withIndex().forEach { (index, letter) ->
        put(letter, Height(index))
    }
    put('S', StartHeight)
    put('E', EndHeight)
}

fun Char.height() = letterToHeight[this] ?: throw IllegalArgumentException("Invalid character: $this")

fun <T> List<List<T>>.find(predicate: (T) -> Boolean): Pair<Int, Int>? {
    forEachIndexed { y, row ->
        row.forEachIndexed { x, value ->
            if (predicate(value)) {
                return x to y
            }
        }
    }
    return null
}

class Grid(val grid: List<List<Height>>) {
    val start = grid.find { it is StartHeight }!!.let { (x, y) -> Position(x, y, this) }
    val end = grid.find { it is EndHeight }!!.let { (x, y) -> Position(x, y, this) }
    private val horizontalRange = grid[0].indices
    private val verticalRange = grid.indices

    operator fun get(x: Int, y: Int): Height {
        return grid[y][x]
    }

    fun inGrid(x: Int, y: Int) = x in horizontalRange && y in verticalRange

}

data class Position(val x: Int, val y: Int, val grid: Grid) {
    private val height: Height
        get() = grid.grid[y][x]

    private fun move(direction: MoveDirection): Position? {
        return when (direction) {
            MoveDirection.UP -> createChecked(x, y - 1, grid)
            MoveDirection.DOWN -> createChecked(x, y + 1, grid)
            MoveDirection.LEFT -> createChecked(x - 1, y, grid)
            MoveDirection.RIGHT -> createChecked(x + 1, y, grid)
        }
    }

    override fun toString(): String {
        return "day12.Position(x=$x, y=$y)"
    }

    val possibleMoves by lazy(LazyThreadSafetyMode.NONE) {
        enumValues<MoveDirection>()
            .mapNotNull { dir -> move(dir) }
            .filter { pos -> pos.height.canMoveTo(height) }
    }

    companion object {
        fun createChecked(x: Int, y: Int, grid: Grid): Position? {
            return if (grid.inGrid(x, y)) {
                Position(x, y, grid)
            } else {
                null
            }
        }
    }
}

class Node private constructor(val value: Position, var visited: Boolean, private val cache: MutableMap<Position, Node>) {
    var parent = null as Node?
    val children by lazy(LazyThreadSafetyMode.NONE) { value.possibleMoves.map { cache(it) } }

    companion object {
        operator fun MutableMap<Position, Node>.invoke(position: Position, visited: Boolean = false): Node {
            return getOrPut(position) { Node(position, visited, this) }
        }
    }
}

fun Position.toNode(map: MutableMap<Position, Node>, visited: Boolean = false) = map(this, visited)
val Node.parentSequence get() = generateSequence(this) { it.parent }
fun Node.buildPath() = parentSequence.map { it.value }.toList().reversed()
fun Node.pathLength() = parentSequence.count() - 1
fun Position.shortestPath(
    predicate: (Position) -> Boolean
): Node? {
    val map = mutableMapOf<Position, Node>()
    // Breadth-first search
    val node = this.toNode(map, true)
    val queue = mutableListOf(node)
    while (queue.isNotEmpty()) {
        val current = queue.removeAt(0)
        if (predicate(current.value)) {
            return current
        }
        current.children
            .filter { !it.visited }
            .forEach {
                it.visited = true
                it.parent = current
                queue.add(it)
            }
    }
    return null
}

fun main() {
    fun part1(input: List<String>): Int {
        val grid = Grid(input.map { it.map { char -> char.height() } })
        val path = grid.start.shortestPath { it == grid.end }!!
        return path.pathLength()
    }

    fun part2(input: List<String>): Int {
        val grid = Grid(input.map { it.map { char -> char.height() } })
        return grid.grid.xyv()
            .mapNotNull { (x, y, value) ->
                if (value.value != 0) return@mapNotNull null

                Position.createChecked(x, y, grid)
            }
            .mapNotNull { it.shortestPath { pos -> pos == grid.end } }
            .minOf { it.pathLength() }
    }

    val testInput = readInput(12, true)
    part1(testInput).let { check(it == 31) { println(it) } }
    part2(testInput).let { check(it == 29) { println(it) } }
    println("Tests passed")

    val input = readInput(12)
    println(part1(input))
    println(part2(input))
}