package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

class Glyph(val project: Unifont, val codepoint: Int, var image: BufferedImage = createGlyphImage(project.settings.size, project.settings.size),
    var missing: Boolean = false, var attributes: MutableMap<GlyphAttribute, String> = mutableMapOf(),
    var tags: MutableMap<GlyphTag, Int> = mutableMapOf()) {

    val character: String = String(Character.toChars(codepoint))

    val width: Int
        get() = image.width
    val height: Int
        get() = image.height

    var advance: Int
        get() = attributes[GlyphAttribute.ADVANCE]?.toIntOrNull() ?: 0
        set(value) { attributes[GlyphAttribute.ADVANCE] = value.toString() }
    var leftBearing: Int
        get() = attributes[GlyphAttribute.LEFT_BEARING]?.toIntOrNull() ?: 0
        set(value) {
            if(value == 0)
                attributes.remove(GlyphAttribute.LEFT_BEARING)
            else
                attributes[GlyphAttribute.LEFT_BEARING] = value.toString()
        }
    var noAutoKern: Boolean
        get() = attributes[GlyphAttribute.NO_AUTO_KERN] != null
        set(value) {
            if(value)
               attributes[GlyphAttribute.NO_AUTO_KERN] = "yes"
            else
               attributes.remove(GlyphAttribute.NO_AUTO_KERN)
        }

    var name: String
        get() = attributes[GlyphAttribute.NAME] ?: "?"
        set(value) { attributes[GlyphAttribute.NAME] = value }

    init {
        if(width % 4 != 0)
            throw IllegalArgumentException("Glyph width not a multiple of 4, it cannot be expressed in hex")
    }

    fun markDirty() {
        project.fileForCodepoint(codepoint).markDirty()
    }

    fun write(): String {
        val glyphHex: String
        if(missing) {
            glyphHex = "0".repeat(8*8/4)
        } else {
            val data = (image.raster.dataBuffer as DataBufferByte).data
            glyphHex = data.joinToString("") { "%02X".format(it) }
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

    operator fun get(x: Int, y: Int): Boolean {
        if(x !in 0 until image.width || y !in 0 until image.height) {
            return false
        }
        val color = image.getRGB(x, y)
        return color == colorModel.getRGB(1)
    }

    operator fun set(x: Int, y: Int, value: Boolean) {
        if(x !in 0 until image.width || y !in 0 until image.height) {
            return
        }
        image.setRGB(x, y, colorModel.getRGB(if(value) 1 else 0))
    }

    fun defaultAdvance(): Int {
        var width = 0
        for(x in 0 until image.width) {
            for(y in 0 until image.height) {
                if(image.getRGB(x, y).toUInt() and 0xFF000000u != 0u)
                    width = max(width, x+1)
            }
        }
        return if(width == 0) 0 else width + 1
    }

    companion object {
        var colorModel = IndexColorModel(Color(1f, 1f, 1f, 0f), Color.BLACK)
        val MISSING_PREFIX = "; Missing >"

        val attrRegex = """(\w+)(?:\s*=\s*(?:([^"]\S*|".+?[^\\]")))?""".toRegex()

        fun read(project: Unifont, line: String): Glyph {
            @Suppress("NAME_SHADOWING")
            var line = line

            val missing = line.startsWith(MISSING_PREFIX)
            if(missing) {
                line = line.removePrefix(MISSING_PREFIX)
            }

            val legacyGlyph = line.until(';')
            val metaString = line.after(';')

            val codepoint = legacyGlyph.until(':').toInt(16)
            val glyphHex = legacyGlyph.after(':') ?: "0".repeat(8*8/4)
            if(glyphHex.isEmpty()) "0".repeat(8*8/4)

            val attributes = mutableMapOf<GlyphAttribute, String>()
            val flags = mutableMapOf<GlyphTag, Int>()

            if(metaString != null) {
                attrRegex.findAll(metaString).forEach { match ->
                    val name = match.groups[1]!!.value
                    var value = match.groups[2]?.value
                    if(value != null) {
                        if(value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length-1)
                        }
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

            val image = createGlyphImage(project.settings.size, project.settings.size)
            val glyph = Glyph(project, codepoint, image, missing, attributes, flags)

            val data = (image.raster.dataBuffer as DataBufferByte).data
            val bytes = glyphHex.chunked(2).map { it.toInt(16).toByte() }.toTypedArray().toByteArray()
            System.arraycopy(bytes, 0, data, 0, bytes.size)

            if(GlyphAttribute.ADVANCE !in glyph.attributes) {
                glyph.advance = glyph.defaultAdvance()
            }
            return glyph
        }

        fun createGlyphImage(width: Int, height: Int) =
            BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, colorModel)
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
        val ADVANCE = GlyphAttribute["advance"]
        val LEFT_BEARING = GlyphAttribute["left_bearing"]
        val NO_AUTO_KERN = GlyphAttribute["no_auto_kern"]
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
