package day15

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import readInput
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

val sensorBeaconRegex =
    Regex("""Sensor at x=(?<sensorX>-?\d+), y=(?<sensorY>-?\d+): closest beacon is at x=(?<beaconX>-?\d+), y=(?<beaconY>-?\d+)""")

fun String.parsePositions(): Pair<Position, Position> {
    val match = sensorBeaconRegex.matchEntire(this) ?: throw IllegalArgumentException("Invalid input: $this")
    val sensorX = match.groups["sensorX"]!!.value.toInt()
    val sensorY = match.groups["sensorY"]!!.value.toInt()
    val beaconX = match.groups["beaconX"]!!.value.toInt()
    val beaconY = match.groups["beaconY"]!!.value.toInt()
    return Position(sensorX, sensorY) to Position(beaconX, beaconY)
}

data class Position(val x: Int, val y: Int) {
    fun distanceTo(other: Position): Int {
        return abs(x - other.x) + abs(y - other.y)
    }

    fun distanceTo(x: Int, y: Int): Int {
        return abs(this.x - x) + abs(this.y - y)
    }
}

data class Signal(val sensor: Position, val signal: Int) {
    fun isInRange(x: Int, y: Int): Boolean {
        return sensor.distanceTo(x, y) <= signal
    }

    companion object {
        operator fun invoke(sensor: Position, beacon: Position): Signal {
            val signal = sensor.distanceTo(beacon)
            return Signal(sensor, signal)
        }
    }
}

class World {
    var xRange = IntRange.EMPTY
    var yRange = IntRange.EMPTY
    private val signals = mutableSetOf<Signal>()
    private val beacons = mutableSetOf<Position>()

    fun addSignal(sensor: Position, beacon: Position) {
        val distance = sensor.distanceTo(beacon)
        val minX = sensor.x - distance
        val maxX = sensor.x + distance
        val minY = sensor.y - distance
        val maxY = sensor.y + distance
        xRange = min(xRange.first, minX)..max(xRange.last, maxX)
        yRange = min(yRange.first, minY)..max(yRange.last, maxY)

        signals.add(Signal(sensor, distance))
        beacons.add(beacon)
    }

    enum class Property {
        UNKNOWN, BEACON, NOT_BEACON
    }

    fun propertyOf(x: Int, y: Int): Property = when {
        beacons.contains(Position(x, y)) -> Property.BEACON
        signals.any { it.isInRange(x, y) } -> Property.NOT_BEACON
        else -> Property.UNKNOWN
    }
}

fun Int.around(distance: Int): IntRange {
    return (this - distance)..(this + distance)
}

class RowWorld(private val row: Int) {
    val ranges = mutableSetOf<IntRange>()

    fun addSignal(sensor: Position, beacon: Position) {
        // println("Row world $row: Adding signal $sensor -> $beacon")
        val distance = sensor.distanceTo(beacon)
        // Check if it even concerns our row
        if (row !in sensor.y.around(distance)) return
        val yDiff = row - sensor.y
        ranges.add(sensor.x.around(distance - abs(yDiff)))
    }
}

fun MutableCollection<IntRange>.merge() {
    val sorted = this.sortedBy { it.first }
    this.clear()
    var current = sorted.first()
    for (range in sorted.drop(1)) {
        current = if (range.first in current) {
            current.first..max(current.last, range.last)
        } else {
            add(current)
            range
        }
    }
    add(current)
}

fun main() {
    fun part1(input: List<String>, row: Int): Int {
        val world = World()
        input.map(String::parsePositions).forEach { (sensor, beacon) ->
            world.addSignal(sensor, beacon)
        }
        var counter = 0
        for (x in world.xRange) {
            if (world.propertyOf(x, row) == World.Property.NOT_BEACON) {
                counter++
            }
        }
        return counter
    }

    fun part2(input: List<String>, gridSize: Int): Int {
        val detections = input.map(String::parsePositions)
        val channel = Channel<Int>()
        return runBlocking(Dispatchers.Default) {
            val jobs = mutableListOf<Job>()
            for (y in 0..gridSize) {
                launch {
                    val rowWorld = RowWorld(y)
                    detections.forEach { (sensor, beacon) ->
                        rowWorld.addSignal(sensor, beacon)
                    }
                    rowWorld.ranges.merge()
                    if (rowWorld.ranges.size != 1) {
                        println("Row $y: ${rowWorld.ranges}")
                        for (x in 0..gridSize) {
                            if (rowWorld.ranges.none { x in it }) {
                                channel.send(x * 4000000 + y)
                            }
                        }
                    }
                }
            }
            channel.receive().also { jobs.forEach { it.cancel() } }
        }
    }

    val testInput = readInput(15, true)
    // part1(testInput, 10).let { check(it == 26) { println(it) } }
    // println("Test 1 passed")
    part2(testInput, 20).let { check(it == 56000011) { println(it) } }
    val input = readInput(15)

    // println(part1(input, 2000000))
    println(part2(input, 4000000))
}