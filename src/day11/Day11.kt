package day11

import day11.Monkey.Companion.parseMonkeys
import readInput
import java.util.ArrayDeque
import java.util.Queue

data class Monkey(
    val id: MonkeyID,
    val holdingItems: Queue<Item>,
    val itemOperation: (Item) -> Unit,
    val rule: ThrowRule
) {
    fun processItems(divideByThree: Boolean, keepInCheck: Long = 0, inspectCallback: () -> Unit): List<ItemOperation> {
        return buildList {
            while (holdingItems.isNotEmpty()) {
                val item = holdingItems.poll()
                if (keepInCheck > 0) { item.worryLevel %= keepInCheck }
                itemOperation(item)
                if (divideByThree) { item.worryLevel /= 3 }
                add(ItemOperation(item, rule.getMonkeyID(item.worryLevel)))
                inspectCallback()
            }
        }
    }

    companion object {
        private val monkeyIDRegex = Regex("day11.Monkey (\\d+):")
        private val itemRegex = Regex("""Starting items: ((\d+)(, (\d+))*)""")
        private val itemOperationRegex = Regex("""Operation: new = old ([+*]) (\d+|old)""")
        private val conditionRegex = Regex("Test: divisible by (\\d+)")
        private val ifTrueRegex = Regex("If true: throw to monkey (\\d+)")
        private val ifFalseRegex = Regex("If false: throw to monkey (\\d+)")
        private fun String.match(regex: Regex): MatchResult {
            return regex.matchEntire(this.trim())!!
        }

        private fun Queue<String>.parseMonkey(): Monkey {
            val id = poll().match(monkeyIDRegex).groupValues[1].toInt()
            val itemsMatch = poll().match(itemRegex).groupValues[1]
            val items = itemsMatch.split(", ").map { Item(it.toLong()) }.toCollection(ArrayDeque())
            val itemOperation = poll().match(itemOperationRegex).groupValues.let { (_, op, value) ->
                val number = value.toIntOrNull()
                when (op) {
                    "+" -> {
                        if (number == null) {
                            check(value == "old") { "Invalid value: $value" }
                            AddToOld
                        } else {
                            AddTo(number)
                        }
                    }

                    "*" -> {
                        if (number == null) {
                            check(value == "old") { "Invalid value: $value" }
                            MultiplyByOld
                        } else {
                            MultiplyBy(number)
                        }
                    }

                    else -> throw IllegalArgumentException("Unknown operation: $op")
                }
            }
            val rule = ThrowRule(
                poll().match(conditionRegex).groupValues[1].toLong(),
                MonkeyID(poll().match(ifTrueRegex).groupValues[1].toInt()),
                MonkeyID(poll().match(ifFalseRegex).groupValues[1].toInt())
            )
            return Monkey(MonkeyID(id), items, itemOperation, rule)
        }

        fun Queue<String>.parseMonkeys(): List<Monkey> {
            return buildList {
                while (this@parseMonkeys.isNotEmpty()) {
                    val monkey = parseMonkey()
                    add(monkey)
                }
            }
        }

    }
}

data class MultiplyBy(val value: Int): (Item) -> Unit {
    override fun invoke(p1: Item) {
        p1.worryLevel *= value
    }
}

data class AddTo(val value: Int): (Item) -> Unit {
    override fun invoke(p1: Item) {
        p1.worryLevel += value
    }
}

object MultiplyByOld: (Item) -> Unit {
    override fun invoke(p1: Item) {
        p1.worryLevel *= p1.worryLevel
    }

    override fun toString(): String {
        return "Self multiply"
    }
}

object AddToOld: (Item) -> Unit {
    override fun invoke(p1: Item) {
        p1.worryLevel += p1.worryLevel
    }

    override fun toString(): String {
        return "Self add"
    }
}

data class ItemOperation(val item: Item, val toMonkey: MonkeyID)
data class ThrowRule(val modulo: Long, val ifTrue: MonkeyID, val ifFalse: MonkeyID) {
    fun getMonkeyID(worryLevel: Long): MonkeyID {
        return if (worryLevel % modulo == 0L) ifTrue else ifFalse
    }
}

@JvmInline
value class MonkeyID(val id: Int)
data class Item(var worryLevel: Long)

fun lowestCommonMultiple(vararg values: Long): Long {
    var result = 1L
    while (true) {
        if (values.all { result % it == 0L }) {
            return result
        }
        result++
    }
}

fun main() {

    fun part1(input: List<String>): Int {
        val monkeys = input.toCollection(ArrayDeque()).parseMonkeys()
        val counts = mutableMapOf<MonkeyID, Int>()
        monkeys.forEach { counts[it.id] = 0 }
        repeat(20) {
            monkeys.forEach { monkey ->
                val itemOperations = monkey.processItems(true) { counts[monkey.id] = counts[monkey.id]!! + 1 }
                itemOperations.forEach { (item, toMonkey) ->
                    monkeys[toMonkey.id].holdingItems.add(item)
                }
            }
        }
        return counts.entries.toList().sortedByDescending { it.value }.take(2).let { (first, second) ->
            first.value * second.value
        }
    }

    fun part2(input: List<String>): Long {
        val monkeys = input.toCollection(ArrayDeque()).parseMonkeys()
        val counts = mutableMapOf<MonkeyID, Int>()
        monkeys.forEach { counts[it.id] = 0 }
        val lcm = lowestCommonMultiple(*monkeys.map { it.rule.modulo }.toLongArray())
        repeat(10000) {
            val it = it + 1
            monkeys.forEach { monkey ->
                val itemOperations = monkey.processItems(false, lcm) { counts[monkey.id] = counts[monkey.id]!! + 1 }
                itemOperations.forEach { (item, toMonkey) ->
                    monkeys[toMonkey.id].holdingItems.add(item)
                }
            }
            // if (it % 1000 == 0 || it == 1 || it == 20) {
            //     println("Iteration $it")
            //     println(counts)
            // }
        }
        return counts.entries.toList().sortedByDescending { it.value }.take(2).let { (first, second) ->
            first.value.toLong() * second.value.toLong()
        }
    }

    val testInput = readInput(11, true)
    part1(testInput).let { check(it == 10605) { println(it) } }
    part2(testInput).let { check(it == 2713310158) { println(it) } }

    val input = readInput(11)
    println(part1(input))
    println(part2(input))
}