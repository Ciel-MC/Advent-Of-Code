package day13

import day13.ListNode.Companion.toListNode
import day13.Value.Companion.toValue
import readInput
import java.io.File

val file = File("AAA.txt")
fun log(message: Any?) {
    file.appendText("$message\n")
    println(message)
}

sealed interface Value {

    fun isCorrect(other: Value): Boolean?

    companion object {
        fun String.toValue(): Value { // day13.log("Parsing for day13.Value: $this")
            toIntOrNull()?.let { return IntValue(it) }
            return this.toListNode()
        }
    }
}

data class ListNode(val value: Value? = null, val next: ListNode? = null): Iterable<ListNode>, Value {

    override fun iterator() = generateSequence(this) { it.next }.iterator()
    override fun isCorrect(other: Value): Boolean? {
        return when (other) {
            is IntValue -> {
                log("This is a list, but the other is an int, wrap the int in a list and compare")
                this.isCorrect(ListNode(IntValue(other.value)))
            }

            is ListNode -> {
                log("Both are lists, compare them")
                this.isCorrect(other)
            }
        }
    }

    override fun toString(): String {
        return "[${this.joinToString(" -> ") { it.value.toString() }}]"
    }

    companion object {
        private fun fromValues(values: List<Value>): ListNode {
            var current: ListNode? = null
            for (value in values.reversed()) {
                current = ListNode(value, current)
            }
            return current ?: ListNode()
        }

        fun CharSequence.indexOfMatching(open: Char, close: Char, start: Int = 0): Int {
            var openCount = 0
            for (i in start..lastIndex) {
                when (this[i]) {
                    open -> openCount++
                    close -> openCount--
                }
                if (openCount == 0) return i
            }
            return -1
        }

        fun String.toListNode(): ListNode { // day13.log("Parsing `$this`")
            val removeBrackets = this.removeSurrounding("[", "]")
            if (removeBrackets.isEmpty()) return ListNode()
            val segments = mutableListOf<String>()

            run {
                val current = StringBuilder()
                var index = 0
                while (index <= removeBrackets.lastIndex) {
                    when (val char = removeBrackets[index]) {
                        ',' -> {
                            segments.add(current.toString())
                            current.clear()
                        }

                        '[' -> {
                            check(current.isEmpty()) { "Unexpected '[' at index $index" }
                            val closeBracket = index + removeBrackets.substring(index).indexOfMatching('[', ']')
                            segments.add(removeBrackets.substring(index..closeBracket))
                            index = closeBracket + 1
                        }

                        else -> {
                            current.append(char)
                        }
                    } // day13.log("Current: $current, segments: $segments")
                    index++
                }
                if (current.isNotEmpty()) segments.add(current.toString())
            } // day13.log("Segments: $segments")
            return fromValues(segments.map { it.toValue() })
        }
    }

    fun isCorrect(other: ListNode): Boolean? {
        log("Compare $this vs $other")
        if (this.value == null) {
            return if (other.value == null) {
                log("Neither item has a value, undecided")
                null
            } else {
                log("This item has ended, but the other hasn't, so it's correct")
                true
            }
        }
        if (other.value == null) {
            log("The other ended, but this didn't, so it's incorrect")
            return false
        }
        val valueResult = this.value.isCorrect(other.value)
        if (valueResult != null) {
            log("Values has a result: $valueResult")
            return valueResult
        }
        if (this.next == null) {
            return if (other.next == null) {
                log("Both ended, it's undecided")
                null
            } else {
                log("This ended, but the other didn't, so it's correct")
                true
            }
        }
        if (other.next == null) {
            log("The other ended, but this didn't, so it's incorrect")
            return false
        }
        log("Still undecided, checking the next item")
        return this.next.isCorrect(other.next)
    }
}

data class IntValue(val value: Int): Value {
    override fun isCorrect(other: Value): Boolean? {
        log("Compare $this vs $other")
        return when (other) {
            is IntValue -> {
                when (value.compareTo(other.value)) {
                    -1 -> {
                        log("This is less than the other, so it's correct")
                        true
                    }

                    0 -> {
                        log("Two values are equal, so it's undecided")
                        null
                    }

                    1 -> {
                        log("This is greater than the other, so it's incorrect")
                        false
                    }

                    else -> {
                        throw IllegalStateException("Unexpected comparison result")
                    }
                }
            }

            is ListNode -> {
                log("This is an int, but the other is a list, wrap the int in a list and compare")
                ListNode(IntValue(value)).isCorrect(other)
            }
        }
    }

    override fun toString(): String {
        return value.toString()
    }

}

fun ListNode.isDivider(i: Int): Boolean {
    if (this.next != null) return false
    val inner = this.value as? ListNode ?: return false
    if (inner.next != null) return false
    val value = inner.value as? IntValue ?: return false
    return value.value == i
}

fun <T> MutableList<T>.booleanSort(compare: (T, T) -> Boolean): MutableList<T> {
    var index = 0
    while (index < lastIndex) {
        if (compare(this[index], this[index + 1])) {
            index++
        } else {
            swap(index, index + 1) // index = 0
            index = maxOf(0, index - 1)
        }
    }
    return this
}

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    this[index1] = this[index2].also { this[index2] = this[index1] }
}

fun main() {
    fun part1(input: List<String>): Int { // day13.log("[[[]]]".toListNode())
        // day13.log("[[]]".toListNode())
        // return 0
        return input.asSequence().chunked(2).map { (a, b) ->
            (a to b) //.also { day13.log("Building: $it") }
        }.map { (a, b) -> a.toListNode() to b.toListNode() }.withIndex().filter { (_, v) -> // day13.log("Compare:")
            // day13.log(v.first)
            // day13.log(v.second)
            v.first.isCorrect(v.second).also { log(it) }!! //.also { day13.log("day02.Result: $it\n") }
        }.sumOf { it.index + 1 }
    }

    fun part2(input: List<String>): Int {
        return input
            .map { it.toListNode() }.toMutableList()
            .booleanSort { a, b ->
                a.isCorrect(b)!!
            } // .onEach {
            //     file.appendText("$it\n")
            // }
            .asSequence().withIndex()
            .filter { it.value.isDivider(2) || it.value.isDivider(6) } // .onEach { day13.log(it.value) }
            .map { it.index + 1 }.take(2).reduce { a, b -> a * b }
    }

    // val testInput = readInput(13, true)
    // part1(testInput).let { check(it == 13) { println(it) } }
    // part2(testInput).also { println(it) }
    // if (readlnOrNull() == "y") {
    //     day13.log("Tests passed")
    val input = readInput(13) // println(part1(input))
    println(part2(input)) // }
}