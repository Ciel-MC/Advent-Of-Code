package day14

import readInput
sealed class Tile {
    data object Wall: Tile()
    class Sand(private val grid: Grid): Tile(), Iterable<Sand.MoveResult> {
        private var x = 500
        private var y = 0
        init {
            grid[x, y] = this
        }
        enum class MoveResult {
            MOVED, STUCK, DELETED
        }
        fun tick(): MoveResult {
            check(grid[x, y] == this)
            val downResult = checkAndMove(x, y + 1) ?: run {
                 // if the movement is out of bound, we hit the abyss and is deleted
                grid[x, y] = Empty // delete this sand
                return MoveResult.DELETED
            }
            if (downResult) return MoveResult.MOVED
            // If we can't move down, try to move left down
            if (checkAndMove(x-1, y+1) == true) return MoveResult.MOVED
            // If we can't move left down, try to move right down
            if (checkAndMove(x+1, y+1) == true) return MoveResult.MOVED
            // If we can't move right down, we're stuck
            return MoveResult.STUCK
        }

        private fun checkAndMove(x: Int, y: Int): Boolean? {
            val tile = grid[x, y] ?: return null
            if (tile == Empty) {
                grid[x, y] = this
                grid[this.x, this.y] = Empty
                this.x = x
                this.y = y
                return true
            }
            return false
        }

        override fun iterator(): Iterator<MoveResult> = generateSequence(MoveResult.MOVED) {
            if (it == MoveResult.MOVED) tick() else null
        }.iterator().also { it.next() }

        override fun toString(): String {
            return "Sand"
        }
    }
    data object Empty: Tile()
}
enum class Movement {
    LEFT_DOWN, DOWN, RIGHT_DOWN, STATIONARY
}
class Grid(width: Int, height: Int) {
    private val inside: List<MutableList<Tile>> = List(height+1) { MutableList(width+1) { Tile.Empty } }

    private val heightRange = 0..height
    private val widthRange = 0..width

    operator fun get(x: Int, y: Int): Tile? = if (x in widthRange && y in heightRange) inside[y][x] else null
    operator fun set(x: Int, y: Int, tile: Tile) {
        // println("Setting $x, $y to $tile")
        if (x !in widthRange || y !in heightRange) return
        inside[y][x] = tile
    }

    operator fun set(x: Int, ys: IntRange, tile: Tile) = ys.forEach { this[x, it] = tile }

    operator fun set(xs: IntRange, y: Int, tile: Tile) = xs.forEach { this[it, y] = tile }
    override fun toString(): String = inside.map {
        it.joinToString("") { tile ->
            when (tile) {
                Tile.Wall -> "#"
                is Tile.Sand -> "o"
                Tile.Empty -> "."
            }
        }
    }.mapIndexed { index, s -> "${index.toString().padStart(2, '0')} $s" }.joinToString("\n")

}

fun<T: Comparable<T>> ordered(a: T, b: T): Pair<T, T> = if (a < b) a to b else b to a

fun createGrid(input: List<String>, withFloor: Boolean = false): Grid {
    val inputs = input
        .map { it.split(" -> ") }
        .map { it.map { it.split(",").let { (a, b) -> a.toInt() to b.toInt() } } }
        .toMutableList()

    val grid = run {
        val flattened = inputs.flatten()
        var height = flattened.maxBy { it.second }.second
        val width = flattened.maxBy { it.first }.first * 2
        if (withFloor) {
            height += 2
            inputs.add(listOf(0 to height, width to height))
        }
        Grid(width, height)
    }

    inputs.forEach {
        it.windowed(2, partialWindows = false).forEach { (a, b) ->
            if (a.first == b.first) {
                val (y1, y2) = ordered(a.second, b.second)
                grid[a.first, y1..y2] = Tile.Wall
            } else {
                val (x1, x2) = ordered(a.first, b.first)
                grid[x1..x2, a.second] = Tile.Wall
            }
        }
    }
    return grid
}

fun main() {
    fun part1(input: List<String>): Int {
        val grid = createGrid(input)

        var count = 0
        while (true) {
            val sand = Tile.Sand(grid)
            count++
            var result: Tile.Sand.MoveResult? = null
            sand.forEach {
                result = it
            }
            when(result!!) {
                Tile.Sand.MoveResult.MOVED -> error("This should not happen")
                Tile.Sand.MoveResult.STUCK -> continue
                Tile.Sand.MoveResult.DELETED -> break
            }
        }
        return count-1
    }

    fun part2(input: List<String>): Int {
        val grid = createGrid(input, true)

        var count = 0
        while (true) {
            if (grid[500, 0] != Tile.Empty) break
            val sand = Tile.Sand(grid)
            count++
            var result: Tile.Sand.MoveResult? = null
            sand.forEach {
                result = it
            }
            // println(grid)
            when(result!!) {
                Tile.Sand.MoveResult.MOVED -> error("This should not happen")
                Tile.Sand.MoveResult.STUCK -> continue
                Tile.Sand.MoveResult.DELETED -> error("This should not happen either")
            }
        }
        return count
    }

    val testInput = readInput(14, true)
    part1(testInput).let { check(it == 24) { println(it) } }
    part2(testInput).let { check(it == 93) { println(it) } }
    val input = readInput(14)

    println(part1(input))
    println(part2(input))
}