package co.thecodewarrior.unifontlib.utils

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel
import java.util.*

//==================================================== Image/Color =====================================================

fun Color.toHexString(): String {
    val a = if(alpha != 255) "%02x".format(alpha) else ""
    val r = "%02x".format(red)
    val g = "%02x".format(green)
    val b = "%02x".format(blue)

    return "#$a$r$g$b"
}

fun Color(hex: String): Color {
    val hexStr = hex.removePrefix("#")
    val a: Int
    val r: Int
    val g: Int
    val b: Int

    when(hexStr.length) {
        3 -> {
            a = 255
            r = "${hexStr[0]}${hexStr[0]}".toInt(16)
            g = "${hexStr[1]}${hexStr[1]}".toInt(16)
            b = "${hexStr[2]}${hexStr[2]}".toInt(16)
        }
        4 -> {
            a = "${hexStr[0]}${hexStr[0]}".toInt(16)
            r = "${hexStr[1]}${hexStr[1]}".toInt(16)
            g = "${hexStr[2]}${hexStr[2]}".toInt(16)
            b = "${hexStr[3]}${hexStr[3]}".toInt(16)
        }
        6 -> {
            a = 255
            r = hexStr.substring(0..1).toInt(16)
            g = hexStr.substring(2..3).toInt(16)
            b = hexStr.substring(4..5).toInt(16)
        }
        8 -> {
            a = hexStr.substring(0..1).toInt(16)
            r = hexStr.substring(2..3).toInt(16)
            g = hexStr.substring(4..5).toInt(16)
            b = hexStr.substring(6..7).toInt(16)
        }
        else ->
            throw IllegalArgumentException("Hex value '$hex' is not valid hex " +
                "(valid formats are #RGB, #ARGB, #RRGGBB, and #AARRGGBB)")
    }

    return Color(r, g, b, a)
}

fun IndexColorModel(vararg palette: Color): IndexColorModel {
    if(palette.size < 2 || palette.size > 65536) {
        throw IllegalArgumentException("Palette size ${palette.size} not in range [2, 65536].")
    }
    var bits = 0
    var maxIndex = palette.size-1
    while(maxIndex != 0) {
        bits++
        maxIndex = maxIndex ushr 1
    }

    val reds   = palette.map { it.red.toByte()   }.toByteArray()
    val greens = palette.map { it.green.toByte() }.toByteArray()
    val blues  = palette.map { it.blue.toByte()  }.toByteArray()
    val alphas = palette.map { it.alpha.toByte() }.toByteArray()
    return IndexColorModel(bits, palette.size, reds, greens, blues, alphas)
}

fun byteArrayOf(vararg ints: Int): ByteArray {
    return ints.map { it.toByte() }.toByteArray()
}

fun BufferedImage.isColor(x: Int, y: Int, color: Color): Boolean {
    return this.getRGB(x, y) == color.rgb
}

fun BufferedImage.isColor(startX: Int, startY: Int, width: Int, height: Int, color: Color): Boolean {
    val array = IntArray(width*height)
    this.getRGB(startX, startY, width, height, array, 0, width)
    return array.all { it == color.rgb }
}

fun BufferedImage.pixels(): Sequence<Pixel> {
    return PixelSequence(this)
}

data class Pixel(val x: Int, val y: Int, val color: Color)

class PixelSequence(val image: BufferedImage): Sequence<Pixel> {
    override fun iterator(): Iterator<Pixel> {
        return PixelIterator()
    }

    private inner class PixelIterator: Iterator<Pixel> {
        private var i = 0

        override fun hasNext(): Boolean {
            return i < image.width * image.height
        }

        override fun next(): Pixel {
            val x = i % image.width
            val y = i / image.width
            i++
            return Pixel(x, y, Color(image.getRGB(x, y)))
        }
    }
}

//================================================ Comparable Utilities ================================================

/**
 * Returns the min of [a] and [b], preferring [a] in the case where they are equal
 */
fun <T: Comparable<T>> min(a: T, b: T): T {
    if(b < a) return b
    return a
}

/**
 * Returns the max of [a] and [b], preferring [a] in the case where they are equal
 */
fun <T: Comparable<T>> max(a: T, b: T): T {
    if(b > a) return b
    return a
}

//================================================== String Utilities ==================================================

fun String.until(str: String): String {
    return this.split(str, limit=2)[0]
}

fun String.after(str: String): String? {
    return this.split(str, limit=2).getOrNull(1)
}

fun String.until(chr: Char): String {
    return this.split(chr, limit=2)[0]
}

fun String.after(chr: Char): String? {
    return this.split(chr, limit=2).getOrNull(1)
}

fun String.until(regex: Regex): String {
    return this.split(regex, limit=2)[0]
}

fun String.after(regex: Regex): String? {
    return this.split(regex, limit=2).getOrNull(1)
}

//============================================ Codepoint-String conversion =============================================

fun Int.codepointHex(): String {
    val digits = if(this > 0xFFFF) 6 else 4
    return "%0${digits}X".format(this)
}

fun IntRange.codepointRange(): String {
    return this.start.codepointHex() + ".." + this.endInclusive.codepointHex()
}

fun String.codepointRange(): IntRange {
    val s = this.split("..", limit=2).map { it.toInt(16) }
    return s[0]..s.getOrElse(1) { s[0] }
}

//================================================= Continuous Ranges ==================================================

fun Collection<Int>.getContinuousRanges(): List<IntRange> {
    val sorted = this.sorted()
    if(sorted.isEmpty()) return listOf()
    val ranges = mutableListOf<IntRange>()

    var start = sorted[0]
    var last = sorted[0]
    sorted.forEach { current ->
        if(current > last+1) {
            ranges.add(start..last)
            start = current
        }

        last = current
    }
    if(start != last)
        ranges.add(start..last)

    return ranges
}

fun ClosedRange<Int>.toIntRange() = this.start..this.endInclusive
fun ClosedRange<Long>.toLongRange() = this.start..this.endInclusive

fun <T: Comparable<T>> ClosedRange<T>.overlaps(other: ClosedRange<T>): Boolean {
    if(this.contains(other.start) || this.contains(other.endInclusive) ||
            other.contains(this.start) || other.contains(this.endInclusive))
        return true
    return false
}

//======================================================= BitSet =======================================================

fun String.hexToBitSet(): BitSet {
    val bitset = BitSet(this.length*4)
    this.forEachIndexed { i, c ->
        val digit = "$c".toInt(16)
        bitset[i*4+0] = digit and 0b1000 != 0
        bitset[i*4+1] = digit and 0b0100 != 0
        bitset[i*4+2] = digit and 0b0010 != 0
        bitset[i*4+3] = digit and 0b0001 != 0
    }
    return bitset
}

fun BitSet.toHex(): String {
    if(this.size() % 4 != 0)
        throw IllegalArgumentException("Bitset with size not a multiple of 4 can't be converted to hex")
    var str = ""
    (0 until this.size()/4).forEach { i ->
        var digit = 0
        digit = digit or if(this[i*4+0]) 0b1000 else 0
        digit = digit or if(this[i*4+1]) 0b0100 else 0
        digit = digit or if(this[i*4+2]) 0b0010 else 0
        digit = digit or if(this[i*4+3]) 0b0001 else 0
        str += digit.toString(16)
    }
    return str
}

fun String.binaryToBitSet(): BitSet {
    val bitset = BitSet(this.length)
    this.forEachIndexed { i, c ->
        when(c) {
            '0' -> bitset[i] = false
            '1' -> bitset[i] = true
            else -> throw IllegalArgumentException("$this is not a valid binary string")
        }
    }
    return bitset
}

fun BitSet.toBinary(): String {
    var str = ""
    (0 until this.size()).forEach { i ->
        str += if(this[i]) '1' else '0'
    }
    return str
}

//==================================================== Random crap =====================================================

