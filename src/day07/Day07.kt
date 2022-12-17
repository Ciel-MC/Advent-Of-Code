package day07

import readInput

sealed interface FileItem {
    val name: String
    val size: Int
    var parent: Folder?

    fun visit(visitor: (FileItem) -> Unit)
}

data class Folder(override val name: String, private val children: MutableList<FileItem>, override var parent: Folder?):
    FileItem {
    fun addChild(children: FileItem) {
        children.parent = this
        this.children.add(children)
    }

    fun cd(name: String): Folder {
        if (name == "..") {
            return parent as Folder
        }
        return children.filterIsInstance<Folder>().first { it.name == name }
    }

    override val size: Int
        get() = children.sumOf { it.size }

    fun size(predicate: (Folder) -> Boolean): Int {
        return children.filterIsInstance<Folder>().filter(predicate).sumOf { it.size }
    }

    override fun visit(visitor: (FileItem) -> Unit) {
        visitor(this)
        children.forEach { it.visit(visitor) }
    }
}

data class File(override val name: String, override val size: Int, override var parent: Folder?): FileItem {
    override fun visit(visitor: (FileItem) -> Unit) {
        visitor(this)
    }
}

fun main() {

    fun parse(input: List<String>): Folder {
        val root = Folder("/", mutableListOf(), null)
        var currentFolder = root
        input.forEach { line ->
            if (line.startsWith("$ ")) {
                val command = line.drop(2)
                if (command == "cd /") return@forEach
                when (command.substringBefore(" ")) {
                    "cd" -> {
                        currentFolder = currentFolder.cd(command.substringAfter(" "))
                    }

                    "ls" -> {}
                }
            } else {
                val type = line.substringBefore(" ")
                if (type == "dir") {
                    val name = line.substringAfter(" ")
                    currentFolder.addChild(Folder(name, mutableListOf(), currentFolder))
                } else {
                    val name = line.substringAfter(" ")
                    val size = type.toInt()
                    currentFolder.addChild(File(name, size, currentFolder))
                }
            }
        }
        return root
    }

    fun part1(input: List<String>): Int {
        val root = parse(input)
        var sum = 0
        root.visit {
            if (it is Folder && it.size < 100000) {
                sum += it.size
            }
        }
        return sum
    }

    fun part2(input: List<String>): Int {
        val diskSize = 70000000
        val requiredSize = 30000000
        val root = parse(input)
        val freeSpace = diskSize - root.size
        val needed = requiredSize - freeSpace
        var smallestDir = Int.MAX_VALUE
        root.visit { item ->
            if (item is Folder) {
                val size = item.size
                if (size >= smallestDir) return@visit
                // println("Found folder ${item.name} with size $size, which is smaller than $smallestDir")
                if (size >= needed) {
                    smallestDir = size
                }
            }
        }
        return smallestDir
    }

    val testInput = readInput(7, true)
    part1(testInput).let { check(it == 95437) { println(it) } }
    part2(testInput).let { check(it == 24933642) { println(it) } }

    val input = readInput(7)
    println(part1(input))
    println(part2(input))
}
