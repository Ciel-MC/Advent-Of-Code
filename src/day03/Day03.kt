package day03

import readInput

fun main() {

    val charToIntMap =
        (('a'..'z') + ('A'..'Z')).withIndex().associate { (i, c) -> c to i + 1 }

    fun Char.charToInt(): Int {
        return charToIntMap[this]!!
    }

    fun part1(input: List<String>): Int {
        return input.sumOf {
            assert(it.length % 2 == 0)
            val a = it.subSequence(0, it.length / 2)
            it.subSequence(it.length / 2, it.length).first { it in a }.charToInt()
        }
    }

    fun part2(input: List<String>): Int {
        return input.chunked(3).sumOf { (a, b, c) ->
            a.first { it in b && it in c }.charToInt()
        }
    }

    val testInput = readInput(3, true)
    check(part1(testInput) == 157)
    check(part2(testInput) == 70)

    val input = readInput(3)
    println(part1(input))
    println(part2(input))
}
