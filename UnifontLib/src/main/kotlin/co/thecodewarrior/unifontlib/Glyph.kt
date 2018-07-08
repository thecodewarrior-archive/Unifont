package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.IndexColorModel
import co.thecodewarrior.unifontlib.utils.Tokenizer
import co.thecodewarrior.unifontlib.utils.codepointHex
import co.thecodewarrior.unifontlib.utils.isColor
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.floor

class Glyph(val codepoint: Int, var image: BufferedImage,
            var attributes: MutableMap<GlyphAttribute, MutableList<String>>, var tags: MutableMap<GlyphTag, Int>,
            var name: String?) {

    val width: Int
        get() = image.width
    val height: Int
        get() = image.height

    init {
        if(width % 4 != 0)
            throw IllegalArgumentException("Glyph width not a multiple of 4, it cannot be expressed in hex")
    }

    fun write(): String {
        val glyphHex = (0 until height).map { y ->
            var row = 0L
            for(x in 0 until width) {
                val mask = 1L shl (width-1-x)
                val pixelSet = image.isColor(x, y, Color.BLACK)
                if(pixelSet) {
                    row = row or mask
                }
            }
            row
        }.joinToString("") {
            it.toString(16).padStart(width/4, '0').toUpperCase()
        }

        val attributesString = attributes.map {
            "${'$'}${it.key.name}=${it.value}"
        }.sorted().joinToString(" ")
        val tagsString = tags.map {
            "${'$'}${it.key.name}".repeat(it.value)
        }.sorted().joinToString(" ")

        return "${codepoint.codepointHex()}:$glyphHex $tagsString $attributesString * $name"
                .replace("\\s+".toRegex(), " ")
    }

    companion object {
        val COLOR_MODEL = IndexColorModel(Color(1f, 1f, 1f, 0f), Color.BLACK)

        fun read(line: String): Glyph {
            val tokenizer = Tokenizer(line)
            // [codepoint]:[glyph] $[flag] $[attr]=[value] $[attr]=[value1],[value2] * [Glyph name]

            val codepoint = tokenizer.until(':').toInt(16)
            val glyphHex = tokenizer.until("\\s".toRegex())
            val metaString = tokenizer.untilOrRemaining("*")
            val name = tokenizer.remaining()

            val attributes = mutableMapOf<GlyphAttribute, MutableList<String>>()
            val flags = mutableMapOf<GlyphTag, Int>()

            metaString.split("\\s+".toRegex()).forEach {
                val parts = it.split('=', limit=2)
                if(parts.size == 2) {
                    val attribute = GlyphAttribute[parts[0].removePrefix("$")]
                    attributes.getOrPut(attribute) { mutableListOf() }.add(parts[0])
                } else {
                    val tag = GlyphTag[parts[0].removePrefix("$")]
                    flags[tag] = flags.getOrDefault(tag, 0)
                }
            }

            if(glyphHex.isEmpty() || glyphHex.any { it !in "0123456789abcdefABCDEF" })
                throw IllegalArgumentException("Glyph string `$glyphHex` is not valid hex")
            val width = floor(glyphHex.length*4.0/16).toInt()


            val image = BufferedImage(glyphHex.length*4/16, 16, BufferedImage.TYPE_BYTE_INDEXED, COLOR_MODEL)
            val glyph = Glyph(codepoint, image, attributes, flags, name)
            val rows = glyphHex.chunked(glyphHex.length).map { it.toLong(16) }

            for (x in 0 until width) {
                for (y in 0 until 16) {
                    val mask = 1L shl (width-1-x)
                    val pixelSet = rows[y] and mask != 0L
                    glyph.image.setRGB(x, y, if(pixelSet) Color.BLACK.rgb else Color(1f, 1f, 1f, 0f).rgb)
                }
            }

            return glyph
        }
    }
}

class GlyphAttribute private constructor(val name: String) {
    companion object {
        val BLANK_WIDTH = GlyphAttribute["blank_width"]
        val COMBINING = GlyphAttribute["combining"]
        val WIDTH_OVERRIDE = GlyphAttribute["width_override"]

        private val map = mutableMapOf<String, GlyphAttribute>()
        operator fun get(name: String): GlyphAttribute {
            return map.getOrPut(name) { GlyphAttribute(name) }
        }
    }
}
class GlyphTag private constructor(val name: String) {
    companion object {
        val NONPRINTING = GlyphTag["np"]
        val IGNORE_EMPTY = GlyphTag["ignore_empty"]

        private val map = mutableMapOf<String, GlyphTag>()
        operator fun get(name: String): GlyphTag {
            return map.getOrPut(name) { GlyphTag(name) }
        }
    }
}
