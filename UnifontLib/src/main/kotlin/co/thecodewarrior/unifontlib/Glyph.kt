package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.*
import java.awt.Color
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import kotlin.reflect.KProperty

class Glyph(val project: Unifont, val codepoint: Int, var image: BufferedImage = createGlyphImage(project.settings.size, project.settings.size),
    var missing: Boolean = false,
    private var attributes: MutableMap<String, String> = mutableMapOf(),
    private var tags: MutableSet<String> = mutableSetOf()
) {

    val character: String = String(Character.toChars(codepoint))

    val bounds: Rect
        get() {
            var minX = image.width
            var minY = image.height
            var maxX = 0
            var maxY = 0
            var empty = true
            image.pixels().filter { it.color == onColor }.forEach {
                empty = false
                minX = min(minX, it.x)
                minY = min(minY, it.y)
                maxX = max(maxX, it.x)
                maxY = max(maxY, it.y)
            }
            if(empty) return Rect(0, 0, 0, 0)
            return Rect(minX, minY, maxX - minX + 1, maxY - minY + 1)
        }

    var advance: Int by GlyphAttributeInt(this, "advance", 0)
    var leftBearing: Int by GlyphAttributeInt(this, "left_bearing", 0)
    var noAutoKern: Boolean by Tag("no_auto_kern")

    var leftProfile: Int by GlyphAttributeInt(this, "left_profile", -1)
    var rightProfile: Int by GlyphAttributeInt(this, "right_profile", -1)

    var leftClass: String? by GlyphAttributeString(this, "left_class", null)
    var rightClass: String? by GlyphAttributeString(this, "right_class", null)
    var name: String by GlyphAttributeString(this, "name", "?")

    var rightBearing: Int
        get() = advance - leftBearing - bounds.maxX
        set(value) {
            advance = value + leftBearing + bounds.maxX
        }
    init {
        if(image.width % 4 != 0)
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
            shouldQuote = shouldQuote || it.key == "name"

            val value = if(shouldQuote) "\"${it.value}\"" else it.value
            " ${it.key}=$value"
        }.sorted().joinToString("")
        val tagsString = tags.sorted().joinToString("")

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
        val onColor = Color.BLACK
        val offColor = Color(1f, 1f, 1f, 0f)
        val colorModel = IndexColorModel(offColor, onColor)

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

            val attributes = mutableMapOf<String, String>()
            val flags = mutableSetOf<String>()

            if(metaString != null) {
                attrRegex.findAll(metaString).forEach { match ->
                    val name = match.groups[1]!!.value
                    var value = match.groups[2]?.value
                    if(value != null) {
                        if(value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length-1)
                        }
                        attributes[name] = value
                    } else {
                        flags.add(name)
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

            return glyph
        }

        fun createGlyphImage(width: Int, height: Int) =
            BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, colorModel)
    }

    abstract class GlyphAttribute<T>(val glyph: Glyph, val name: String, val defaultValue: T) {
        private var cached: T = defaultValue

        init {
            glyph.attributes[name]?.also {
                cached = parse(it) ?: defaultValue
            }
        }

        abstract fun parse(value: String): T?
        abstract fun save(value: T): String?

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T = cached

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            cached = value
            val saved = save(value)
            if(saved == null) {
                glyph.attributes.remove(name)
            } else {
                glyph.attributes[name] = saved
            }
        }

//        val BLANK_WIDTH = GlyphAttribute["blank_width"]
//        val WIDTH_OVERRIDE = GlyphAttribute["width_override"]
//        val NAME = GlyphAttribute["name"]
//        val ADVANCE = GlyphAttribute["advance"]
//        val LEFT_BEARING = GlyphAttribute["left_bearing"]
//        val NO_AUTO_KERN = GlyphAttribute["no_auto_kern"]
//        val LEFT_CLASS = GlyphAttribute["left_class"]
//        val RIGHT_CLASS = GlyphAttribute["right_class"]
//        val LEFT_PROFILE = GlyphAttribute["left_profile"]
//        val RIGHT_PROFILE = GlyphAttribute["right_profile"]
    }

    class GlyphAttributeString<S: String?>(glyph: Glyph, name: String, defaultValue: S): GlyphAttribute<S>(glyph, name, defaultValue) {
        @Suppress("UNCHECKED_CAST")
        override fun parse(value: String): S {
            return value as S
        }

        override fun save(value: S): String? {
            return value
        }
    }

    class GlyphAttributeInt(glyph: Glyph, name: String, defaultValue: Int): GlyphAttribute<Int>(glyph, name, defaultValue) {
        @Suppress("UNCHECKED_CAST")
        override fun parse(value: String): Int? {
            return value.toIntOrNull()
        }

        override fun save(value: Int): String? {
            return value.toString()
        }
    }

    class Tag(val name: String) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
            thisRef as Glyph
            return name in thisRef.tags
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            thisRef as Glyph
            if(value) {
                thisRef.tags.add(name)
            } else {
                thisRef.tags.remove(name)
            }
        }
    }
}

