package day04

import readInput

fun main() {

    fun String.toRange(): IntRange {
        val (min, max) = this.split("-")
        return min.toInt() .. max.toInt()
    }

    fun String.toIntRanges(): Pair<IntRange, IntRange> {
        return this.split(",").let { (a, b) -> a.toRange() to b.toRange() }
    }

    operator fun IntRange.contains(other: IntRange): Boolean {
        return this.contains(other.first) && this.contains(other.last)
    }

    fun part1(input: List<String>): Int {
        return input.map(String::toIntRanges)
            .count { (a, b) -> a in b || b in a }
    }

    fun part2(input: List<String>): Int {
        return input.map(String::toIntRanges)
            .count { (a, b) ->
                a.firstOrNull { it in b } != null
            }
    }

    val testInput = readInput(4, true)
    check(part1(testInput) == 2)
    check(part2(testInput) == 4)

    val input = readInput(4)
    println(part1(input))
    println(part2(input))
}
