package day02

import day02.Move.*
import day02.Result.*
import readInput

enum class Move(val score: Int) {
    ROCK(1), PAPER(2), SCISSORS(3);

    fun beats(other: Move): Result {
        return when (this to other) {
            ROCK to SCISSORS, PAPER to ROCK, SCISSORS to PAPER -> WIN
            ROCK to PAPER, PAPER to SCISSORS, SCISSORS to ROCK -> LOSE
            else -> DRAW
        }
    }

    companion object {
        fun from(s: String): Move {
            return when (s) {
                "A", "X" -> ROCK
                "B", "Y" -> PAPER
                "C", "Z" -> SCISSORS
                else -> throw IllegalArgumentException("Invalid move: $s")
            }
        }
    }
}

enum class Result(val score: Int) {
    WIN(6), LOSE(0), DRAW(3);

    companion object {
        fun from(s: String): Result {
            return when (s) {
                "X" -> LOSE
                "Y" -> DRAW
                "Z" -> WIN
                else -> throw IllegalArgumentException("Invalid result: $s")
            }
        }
    }
}

fun main() {

    fun part1(input: List<String>): Int {
        return input.map { it.split(" ") }.map { Move.from(it[0]) to Move.from(it[1]) }.sumOf {
            val (opponent, me) = it
            me.beats(opponent).score + me.score
        }
    }

    fun part2(input: List<String>): Int {
        fun moveToMake(opponent: Move, result: Result): Move {
            return when (opponent to result) {
                ROCK to WIN, PAPER to DRAW, SCISSORS to LOSE -> PAPER
                ROCK to DRAW, PAPER to LOSE, SCISSORS to WIN -> ROCK
                ROCK to LOSE, PAPER to WIN, SCISSORS to DRAW -> SCISSORS
                else -> throw IllegalArgumentException("Invalid move: $opponent $result")
            }
        }
        return input.map {
            it.split(" ").let { (opponent, result) -> Move.from(opponent) to Result.from(result) }
        }.sumOf {
                val (opponent, result) = it
                moveToMake(opponent, result).score + result.score
            }
    }

    val testInput = readInput(2, true)
    check(part1(testInput) == 15)
    check(part2(testInput) == 12)

    val input = readInput(2)
    println(part1(input))
    println(part2(input))
}
