package day05

import readInput
import java.util.*
fun main() {

    fun parseCrateStack(input: List<String>): Map<Int, Stack<Char>> {
        return buildMap {
            input.reversed().forEach { row ->
                    row.chunked(4).map { it.trim().removeSurrounding("[", "]") }
                        .map { it.firstOrNull() }
                        .forEachIndexed { index, c ->
                            if (c == null) return@forEachIndexed
                            this.getOrPut(index+1) { Stack() }.push(c)
                        }
                }
        }
    }

    data class Instruction(val amount: Int, val from: Int, val to: Int)

    val instructionRegex = Regex("""move (\d+) from (\d+) to (\d+)""")
    fun parseInstructions(input: String): Instruction {
        val (amount, from, to) = instructionRegex.matchEntire(input)?.destructured ?: error("Invalid input")
        return Instruction(amount.toInt(), from.toInt(), to.toInt())
    }

    fun List<String>.indexOfEndOfStacks(): Int {
        return this.indexOfFirst { "[" !in it }
    }

    fun <T> List<T>.splitAt(index: Int): Pair<List<T>, List<T>> {
        return this.take(index) to this.drop(index)
    }

    fun part1(input: List<String>): String {
        val (stack, instruction) = input.splitAt(input.indexOfEndOfStacks())
        val stacks = parseCrateStack(stack)
        val instructions = instruction.drop(1).map(::parseInstructions)
        instructions.forEach { (amount, from, to) ->
            repeat(amount) {
                stacks[to]!!.push(stacks[from]!!.pop())
            }
        }
        return stacks.entries
            .sortedBy { it.key }
            .map { it.value.peek() }
            .joinToString("")
    }

    fun part2(input: List<String>): String {
        val (stack, instruction) = input.splitAt(input.indexOfEndOfStacks())
        val stacks = parseCrateStack(stack)
        val instructions = instruction.drop(1).map(::parseInstructions)
        val crane = Stack<Char>()
        instructions.forEach { (amount, from, to) ->
            repeat(amount) {
                crane.push(stacks[from]!!.pop())
            }
            repeat(amount) {
                stacks[to]!!.push(crane.pop())
            }
        }
        return stacks.entries
            .sortedBy { it.key }
            .map { it.value.peek() }
            .joinToString("")
    }

    val testInput = readInput(5, true)
    part1(testInput).let { check(it == "CMZ") { println(it) } }
    part2(testInput).let { check(it == "MCD") { println(it) } }

    val input = readInput(5)
    println(part1(input))
    println(part2(input))
}
