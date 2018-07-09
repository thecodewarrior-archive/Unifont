package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.*
import kotlin.math.floor

class Glyph(val codepoint: Int, var image: BufferedImage = createGlyphImage(8,16), var missing: Boolean = false,
            var attributes: MutableMap<GlyphAttribute, String> = mutableMapOf(),
            var tags: MutableMap<GlyphTag, Int> = mutableMapOf()) {

    val width: Int
        get() = image.width
    val height: Int
        get() = image.height

    init {
        if(width % 4 != 0)
            throw IllegalArgumentException("Glyph width not a multiple of 4, it cannot be expressed in hex")
    }

    fun write(): String {
        val glyphHex: String
        if(missing) {
            glyphHex = "0".repeat(8*16/4)
        } else {
            val bitset = BitSet(width*height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    bitset[y*width + x] = image.isColor(x, y, Color.BLACK)
                }
            }

            glyphHex = bitset.toHex()
        }

        val attributesString = attributes.map {
            var shouldQuote = "\\s".toRegex().find(it.value) != null
            shouldQuote = shouldQuote || it.key == GlyphAttribute.NAME

            val value = if(shouldQuote) "\"${it.value}\"" else it.value
            " ${it.key.name}=$value"
        }.sorted().joinToString("")
        val tagsString = tags.map {
            " ${it.key.name}"
        }.sorted().joinToString("")

        val missingPrefix = if(missing) MISSING_PREFIX else ""

        return "$missingPrefix${codepoint.codepointHex()}:$glyphHex;$tagsString$attributesString"
    }

    companion object {
        val COLOR_MODEL = IndexColorModel(Color(1f, 1f, 1f, 0f), Color.BLACK)
        val MISSING_PREFIX = "; Missing >"

        val attrRegex = """(\w+)(?:\s*=\s*(?:([^"]\S*|".+?[^\\]")))?""".toRegex()

        fun read(line: String): Glyph {
            @Suppress("NAME_SHADOWING")
            var line = line

            val missing = line.startsWith(MISSING_PREFIX)
            if(missing) {
                line = line.removePrefix(MISSING_PREFIX)
            }

            val legacyGlyph = line.until(';')
            val metaString = line.after(';')

            val codepoint = legacyGlyph.until(':').toInt(16)
            val glyphHex = legacyGlyph.after(':') ?: "0".repeat(16*8/4)

            val attributes = mutableMapOf<GlyphAttribute, String>()
            val flags = mutableMapOf<GlyphTag, Int>()

            if(metaString != null) {
                attrRegex.findAll(metaString).forEach { match ->
                    val name = match.groups[1]!!.value
                    val value = match.groups[2]?.value
                    if(value != null) {
                        val attribute = GlyphAttribute[name]
                        attributes[attribute] = value
                    } else {
                        val tag = GlyphTag[name]
                        flags[tag] = flags.getOrDefault(tag, 0)
                    }
                }
            }

            if(glyphHex.isEmpty() || glyphHex.any { it !in "0123456789abcdefABCDEF" })
                throw IllegalArgumentException("Glyph string `$glyphHex` is not valid hex")
            val width = floor(glyphHex.length*4.0/16).toInt()

            val image = createGlyphImage(glyphHex.length*4/16, 16)
            val glyph = Glyph(codepoint, image, missing, attributes, flags)
            val bitset = glyphHex.hexToBitSet()

            for (x in 0 until width) {
                for (y in 0 until 16) {
                    glyph.image.setRGB(x, y, if(bitset[y*width + x]) Color.BLACK.rgb else Color(1f, 1f, 1f, 0f).rgb)
                }
            }

            return glyph
        }

        fun createGlyphImage(width: Int, height: Int) = BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, COLOR_MODEL)
    }
}

class GlyphAttribute private constructor(val name: String) {
    companion object {
        private val map = mutableMapOf<String, GlyphAttribute>()
        operator fun get(name: String) = map.getOrPut(name) { GlyphAttribute(name) }

        val BLANK_WIDTH = GlyphAttribute["blank_width"]
        val COMBINING = GlyphAttribute["combining"]
        val WIDTH_OVERRIDE = GlyphAttribute["width_override"]
        val NAME = GlyphAttribute["name"]
    }
}
class GlyphTag private constructor(val name: String) {
    companion object {
        private val map = mutableMapOf<String, GlyphTag>()

        val NONPRINTING = GlyphTag["np"]
        val IGNORE_EMPTY = GlyphTag["ignore_empty"]
        operator fun get(name: String) = map.getOrPut(name) { GlyphTag(name) }
    }
}
