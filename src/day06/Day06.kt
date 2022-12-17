package day06

import readInput

fun main() {

    fun String.findDistinctOf(size: Int): Int {
        return this.windowed(size).withIndex().first { it.value.toSet().size == size }.index + size
    }

    fun part1(input: String): Int {
        return input.findDistinctOf(4)
    }

    fun part2(input: String): Int {
        return input.findDistinctOf(14)
    }

    val testInput = readInput(6, true)
    testInput.drop(1).zip(listOf(5, 6, 10, 11)).forEachIndexed { index, (input, expected) ->
        val actual = part1(input)
        check(actual == expected) { "Test $index failed: expected $expected, actual $actual" }
    }
    testInput.zip(listOf(19, 23, 23, 29, 26)).forEachIndexed { index, (input, expected) ->
        val actual = part2(input)
        check(actual == expected) { "Test $index failed: expected $expected, actual $actual" }
    }

    val input = readInput(6)[0]
    println(part1(input))
    println(part2(input))
}
