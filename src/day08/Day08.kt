package day08

import readInput

fun <T> List<T>.splitExclusive(index: Int): Pair<List<T>, List<T>> = this.take(index) to this.drop(index + 1)

data class Vertical(val x: Int)

operator fun <T> List<List<T>>.get(vertical: Vertical): List<T> = this.map { it[vertical.x] }

fun <T> List<List<T>>.splitExclusiveVertical(y: Int, x: Int): Pair<List<T>, List<T>> =
    this[Vertical(x)].splitExclusive(y)

fun <T> List<List<T>>.xyv(): List<Triple<Int, Int, T>> =
    this.withIndex().flatMap { (i, v) -> v.withIndex().map { i to it } }.map { (y, v) ->
        val (x, value) = v
        Triple(x, y, value)
    }

fun <T> List<T>.indexOfFirstOrLast(predicate: (T) -> Boolean): Int =
    this.indexOfFirst(predicate).takeIf { it != -1 } ?: this.lastIndex

fun main() {
    fun part1(input: List<String>): Int {
        val grid = input.map { string -> string.toCharArray().map(Char::digitToInt) }
        return grid.xyv().count { (x, y, value) ->
            val (left, right) = grid[y].splitExclusive(x)
            val (top, bottom) = grid.splitExclusiveVertical(y, x)
            return@count listOf(left, right, top, bottom).any { it.all { it < value } }
        }
    }

    fun part2(input: List<String>): Int {
        val grid = input.map { string -> string.toCharArray().map { char -> char.digitToInt() } }
        return grid.xyv().maxOf { (x, y, value) ->
            buildList(4) {
                grid[y].splitExclusive(x).let { (l, r) ->
                    add(l.reversed())
                    add(r)
                }
                grid.splitExclusiveVertical(y, x).let { (t, b) ->
                    add(t.reversed())
                    add(b)
                }
            }.map { it.indexOfFirstOrLast { it >= value } + 1 }.reduce(Int::times)
        }
    }

    val testInput = readInput(8, true)
    part1(testInput).let { check(it == 21) { println(it) } }
    part2(testInput).let { check(it == 8) { println(it) } }

    val input = readInput(8)
    println(part1(input))
    println(part2(input))
}