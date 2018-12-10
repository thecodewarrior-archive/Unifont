package co.thecodewarrior.unifontcli.commands.exporters.ttf

import co.thecodewarrior.unifontlib.Glyph
import co.thecodewarrior.unifontlib.GlyphAttribute
import co.thecodewarrior.unifontlib.GlyphTag
import org.doubletype.ossa.Engine
import org.doubletype.ossa.adapter.EContour
import org.doubletype.ossa.adapter.EContourPoint
import org.doubletype.ossa.truetype.TTUnicodeRange
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.max

class UnifontTTFGenerator(val name: String, val outputDir: File) {
    private val px2pt = 1024.0/8
    private fun pt(px: Int) = px * px2pt

    val engine = Engine.getSingletonInstance()
    init {
        outputDir.mkdirs()
        engine.buildNewTypeface(name, outputDir)
    }
    private val typeface = engine.typeface

    init {
        typeface.ascender = pt(6)
        typeface.xHeight = pt(4)
        typeface.descender = pt(2)
        typeface.bottomSideBearing = pt(0)
        typeface.topSideBearing = pt(0)
        typeface.advanceWidth = pt(5).toInt()

        typeface.author = "thecodewarrior"
        typeface.license = "MIT and CC BY 4.0"
    }

    fun build(): Font? {
        try {
            typeface.saveGlyphFile()
            typeface.buildTTF(false)
            return typeface.font
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun addGlyph(glyph: Glyph) {
        val range = TTUnicodeRange.of(glyph.codepoint.toLong())
        if(range != null) typeface.addUnicodeRange(range.toString())
        val glyphFile = typeface.createGlyph(glyph.codepoint.toLong())
        if(GlyphTag.NONPRINTING !in glyph.tags) {
            val leftHang = glyph.attributes[GlyphAttribute.LEFT_HANG]?.toIntOrNull() ?: 0
            val contours = createContours(glyph)
            contours.forEach {
                val econtour = EContour()
                econtour.type = EContour.k_cubic

                for (point in it) {
                    val epoint = EContourPoint(
                        pt(point.x - leftHang),
                        pt(8 - point.y - 2),
                        true
                    )
                    econtour.addContourPoint(epoint)
                }
                glyphFile.addContour(econtour)
            }
            glyphFile.advanceWidth = pt(glyph.advance).toInt()
        } else {
            glyphFile.advanceWidth = pt(glyph.advance).toInt()
        }
        glyph.attributes[GlyphAttribute.BLANK_WIDTH]?.toIntOrNull()?.let {
            glyphFile.advanceWidth = it
        }
        glyph.attributes[GlyphAttribute.WIDTH_OVERRIDE]?.toIntOrNull()?.let {
            glyphFile.advanceWidth = it
        }

        glyphFile.author = "thecodewarrior"
        glyphFile.glyphTitle = "uni%04X".format(glyph.codepoint)

        glyphFile.saveGlyphFile()
        typeface.addGlyph(glyphFile)
    }

    private fun createContours(glyph: Glyph): List<List<PixelPos>> {
        val pixels = mutableMapOf<PixelPos, MutableList<ContourSide>>()
        val image = glyph.image
        for(x in 0 until image.width) {
            for(y in 0 until image.height) {
                val pos = PixelPos(x, y)
                if(image.isOn(pos)) {
                    val sides = ContourSide.values().filter {
                        !image.isOn(pos + it.offset)
                    }.toMutableList()
                    if(sides.isNotEmpty())
                        pixels[PixelPos(x, y)] = sides
                }
            }
        }

        if(pixels.isEmpty()) return listOf()

        val contours = mutableListOf<List<PixelPos>>()
        while(pixels.any { it.value.isNotEmpty() }) {
            var pixel = pixels.entries.first { it.value.isNotEmpty() }.key
            var side = pixels[pixel]?.removeAt(0) ?: continue
            var points = mutableListOf<PixelPos>()

            if(side !in pixels[pixel - side.next.offset] ?: mutableListOf()) {
                // if the point isn't in the middle of a line its corner needs a point
                points.add(pixel + side.cornerOffset)
            }

            while(true) {
                pixels[pixel]?.remove(side)
//                println("Advancing from ${pixel.x},${pixel.y}@$side")
                val tests = listOf(
                    side.next to pixel,
                    side to pixel + side.next.offset,
                    side.prev to pixel + side.next.offset + side.offset
                )

                val result = tests.firstOrNull {
                    it.first in pixels[it.second] ?: mutableListOf()
                } ?: break

                if(result.first != side) {
//                    println("Turning corner from ${pixel.x},${pixel.y}@$side to " +
//                        "${result.second.x},${result.second.y}@${result.first}")
                    points.add(result.second + result.first.cornerOffset)
                    pixel = result.second
                    side = result.first
                } else {
                    pixel = result.second
                }
            }

            contours.add(points)
//            println("[")
//            println(points.joinToString("\n"))
//            println("]")
        }

        return contours
    }

    private fun BufferedImage.isOn(pos: PixelPos): Boolean {
        if(pos.x < 0 || pos.y < 0)
            return false
        if(pos.x >= width || pos.y >= height)
            return false
        return getRGB(pos.x, pos.y) == Color.BLACK.rgb
    }

    private data class PixelPos(val x: Int, val y: Int) {
        operator fun plus(other: PixelPos): PixelPos {
            return PixelPos(x + other.x, y + other.y)
        }

        operator fun minus(other: PixelPos): PixelPos {
            return PixelPos(x - other.x, y - other.y)
        }

        operator fun unaryMinus(): PixelPos {
            return PixelPos(-x, -y)
        }
    }

    private enum class ContourSide(
            /**
             * the offset in this cardinal direction
             */
            val offset: PixelPos,
            /**
             * The offset of the corner anticlockwise from the center with respect to the top left corner (draw a line
             * from the center out through this cardinal side, rotate it anticlockwise until it hits a corner)
             */
            val cornerOffset: PixelPos) {
        TOP(PixelPos(0, -1), PixelPos(0, 0)),
        RIGHT(PixelPos(1, 0), PixelPos(1, 0)),
        BOTTOM(PixelPos(0, 1), PixelPos(1, 1)),
        LEFT(PixelPos(-1, 0), PixelPos(0, 1));

        val next: ContourSide
            get() = ContourSide.values()[(ordinal + 1) % 4]
        val prev: ContourSide
            get() = ContourSide.values()[(ordinal + 4 - 1) % 4]
    }
}