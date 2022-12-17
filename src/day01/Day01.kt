package day01

import readInput

fun main() {
    fun List<String>.toSnacks(): List<Int> {
        var current = 0
        return buildList {
            for (i in this@toSnacks) {
                if (i == "") {
                    add(current)
                    current = 0
                } else {
                    current += i.toInt()
                }
            }
        }
    }

    fun part1(input: List<String>): Int {
        return input.toSnacks().max()
    }

    fun part2(input: List<String>): Int {
        return input.toSnacks().sortedDescending().take(3).sum()
    }

    val input = readInput(1)
    println(part1(input))
    println(part2(input))
}
