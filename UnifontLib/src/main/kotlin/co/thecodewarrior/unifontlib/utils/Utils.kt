package co.thecodewarrior.unifontlib.utils

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel

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

fun Int.codepointHex(): String {
    val digits = if(this > 0xFFFF) 6 else 4
    return "%0${digits}X".format(this)
}

fun codepointsToRanges(codepoints: Collection<Int>): List<IntRange> {
    TODO()
}

fun codepointsToRangeStrings(codepoints: Collection<Int>): List<String> {
    return rangesToStrings(codepointsToRanges(codepoints), transform = { it.codepointHex() })
}

fun rangesToStrings(ranges: Collection<IntRange>,
                    rangeSeparator: String = "..",
                    transform: (Int) -> String = { it.toString() }): List<String> {
    return ranges.map { range ->
        if(range.start == range.endInclusive) {
            transform(range.start)
        } else {
            transform(range.start) + rangeSeparator + transform(range.endInclusive)
        }
    }
}