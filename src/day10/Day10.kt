package day10

import readInput

sealed interface Instruction {
    fun run(state: State): Boolean
    companion object {
        fun parse(line: String): Instruction = when {
            line.startsWith("addx ") -> AddX(line.drop(5).toInt())
            line == "noop" -> NoOp
            else -> throw IllegalArgumentException("Unknown instruction: $line")
        }
    }
}

object NoOp : Instruction {
    override fun run(state: State): Boolean = true
}

data class AddX(val x: Int) : Instruction {
    override fun run(state: State): Boolean {
        return if (state.waited) {
            state.waited = false
            state.value += x
            true
        } else {
            state.waited = true
            false
        }
    }
}
data class State(var value: Int, var waited: Boolean = false)

fun main() {

    fun part1(input: List<String>): Int {
        val state = State(1)
        var accumulator = 0
        val instructions = input.map(Instruction.Companion::parse)
        var index = 1
        var instructionIndex = 0
        while (instructionIndex < instructions.size) {
            val instruction = instructions[instructionIndex]
            if (instruction.run(state)) {
                instructionIndex ++
            }
            index ++

            if ((index - 20) % 40 == 0) {
                accumulator += state.value * index
            }
        }
        return accumulator
    }

    fun part2(input: List<String>) {
        val state = State(1)
        val instructions = input.map(Instruction.Companion::parse)
        var index = 1
        var instructionIndex = 0
        repeat(40) {
            print((it+1).toString().padStart(3, ' '))
        }
        println()
        while (instructionIndex < instructions.size) {
            val instruction = instructions[instructionIndex]
            if (instruction.run(state)) {
                instructionIndex ++
            }

            val currentX = index % 40
            if (currentX in state.value-1..state.value+1) {
                print("#".padStart(3, ' '))
            } else {
                print(".".padStart(3, ' '))
            }
            if (currentX == 0) {
                println()
            }

            index++
        }

    }

    val testInput = readInput(10, true)
    part1(testInput).let { check(it == 13140) { println(it) } }

    val input = readInput(10)
    println(part1(input))
    part2(input)
    // println(part2(input))
}
