package co.thecodewarrior.unifontlib.utils

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel

//==================================================== Image/Color =====================================================

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
    return this.split(str, limit=1)[0]
}

fun String.after(str: String): String? {
    return this.split(str, limit=1).getOrNull(1)
}

fun String.until(chr: Char): String {
    return this.split(chr, limit=1)[0]
}

fun String.after(chr: Char): String? {
    return this.split(chr, limit=1).getOrNull(1)
}

fun String.until(regex: Regex): String {
    return this.split(regex, limit=1)[0]
}

fun String.after(regex: Regex): String? {
    return this.split(regex, limit=1).getOrNull(1)
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
    val s = this.split("..", limit=1).map { it.toInt(16) }
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

    return ranges
}
