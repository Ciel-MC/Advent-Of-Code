import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Reads lines from the given input txt file.
 */
fun readInput(day: Int, test: Boolean = false): List<String> {
    val dayStr = day.toString().padStart(2, '0')
    val fileName = "Day${dayStr}${if (test) "_test" else ""}.txt"
    return File("src/day$dayStr/$fileName").readLines()
}

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')