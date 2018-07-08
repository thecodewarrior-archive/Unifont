package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.*
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.floor

class Glyph(val codepoint: Int, var image: BufferedImage,
            var attributes: MutableMap<GlyphAttribute, MutableList<String>>, var tags: MutableMap<GlyphTag, Int>) {

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

        return "${codepoint.codepointHex()}:$glyphHex; $tagsString $attributesString"
    }

    companion object {
        val COLOR_MODEL = IndexColorModel(Color(1f, 1f, 1f, 0f), Color.BLACK)

        val attrRegex = """(\w+)(?:\s*=\s*(?:([^"]\S*|".+?[^\\]")))?""".toRegex()

        fun read(line: String): Glyph {
            val legacyGlyph = line.until(';')
            val metaString = line.after(';')

            val codepoint = legacyGlyph.until(':').toInt(16)
            val glyphHex = legacyGlyph.after(':') ?: "0".repeat(16*8/4)

            val attributes = mutableMapOf<GlyphAttribute, MutableList<String>>()
            val flags = mutableMapOf<GlyphTag, Int>()

            if(metaString != null) {
                attrRegex.findAll(metaString).forEach { match ->
                    val name = match.groups[1]!!.value
                    val value = match.groups[2]?.value
                    if(value != null) {
                        val attribute = GlyphAttribute[name]
                        attributes.getOrPut(attribute) { mutableListOf() }.add(value)
                    } else {
                        val tag = GlyphTag[name]
                        flags[tag] = flags.getOrDefault(tag, 0)
                    }
                }
            }

            if(glyphHex.isEmpty() || glyphHex.any { it !in "0123456789abcdefABCDEF" })
                throw IllegalArgumentException("Glyph string `$glyphHex` is not valid hex")
            val width = floor(glyphHex.length*4.0/16).toInt()

            val image = BufferedImage(glyphHex.length*4/16, 16, BufferedImage.TYPE_BYTE_INDEXED, COLOR_MODEL)
            val glyph = Glyph(codepoint, image, attributes, flags)
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
        val NAME = GlyphAttribute["name"]

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
