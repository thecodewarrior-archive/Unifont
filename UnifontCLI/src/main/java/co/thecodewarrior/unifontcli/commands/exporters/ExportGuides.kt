package co.thecodewarrior.unifontcli.commands.exporters

import co.thecodewarrior.unifontcli.common.*
import co.thecodewarrior.unifontcli.utils.IndexColorModel
import co.thecodewarrior.unifontcli.utils.drawPixel
import co.thecodewarrior.unifontcli.utils.hex
import co.thecodewarrior.unifontcli.utils.loadWithProgress
import co.thecodewarrior.unifontlib.HexFile
import co.thecodewarrior.unifontlib.utils.overlaps
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.awt.*
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.AffineTransformOp
import java.awt.geom.AffineTransform
import java.awt.Toolkit.getDefaultToolkit



class ExportGuides: Exporter(name="guides") {
    val prefix by option("-p", "--prefix", help = "The codepoint prefix in hex. The output image will contain all the " +
            "codepoints from U+xxxx00 to U+xxxxFF.").hex().default(0)
    val flip by option("-f", "--flip", help = "Flips the glyphs to go left to right, then top to bottom.")
            .flag("-F", "--unflip")
    val reference by option("--reference", help = "Specifies a file to be exported as a reference").file()

    private val referenceScale = 4

    override fun run() {
        val image = BufferedImage(274, 238, BufferedImage.TYPE_BYTE_BINARY,
                IndexColorModel(Color(0xffffff), Color(0x000000), Color(0xffbfbf), Color(0xc0ffff))
        )
        val codepointRange = (prefix shl 8)..(prefix shl 8 or 0xFF)

        val c = Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), floatArrayOf(1f, 1f, 1f), 0f)

        var g = image.graphics
        g.color = Color.WHITE
        g.drawRect(0, 0, image.width, image.height)

        drawMetadata(g)
        drawAxes(g)
        drawGuides(g)

        var referenceImage = BufferedImage(image.width*referenceScale, image.height*referenceScale, BufferedImage.TYPE_INT_ARGB)
        val at = AffineTransform()
        at.scale(referenceScale.toDouble(), referenceScale.toDouble())
        val scaleOp = AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
        referenceImage = scaleOp.filter(image, referenceImage)

        val neededFiles = unifont.filesForCodepoints(codepointRange)
        neededFiles.loadWithProgress()
        neededFiles.forEach {
            drawGlyphs(g, it)
        }

        g.dispose()
        g = referenceImage.graphics

        drawReferenceGlyphs(g)

        g.dispose()

        ImageIO.write(image, file.extension, file)
        reference?.also { ImageIO.write(referenceImage, it.extension, it) }
    }

    private fun drawMetadata(g: Graphics) {
        drawMetadataLine(g, row = 0, value = prefix, bits = 16)
        drawMetadataLine(g, row = 1, value = if(flip) 1 else 0, bits = 1)
    }

    private fun drawMetadataLine(g: Graphics, row: Int, value: Int, bits: Int) {
        val y = 3 + row * 2
        var x = 3

        g.color = Color.BLACK
        g.fillRect(x-1, y-1, bits + 2, 3)

        for(i in 0 until bits) {
            val mask = 1 shl (15-i)
            // off bit = white, on bit = black. Not the other way round because white is background, black is foreground
            val color = if(value and mask == 0) Color.WHITE else Color.BLACK
            g.drawPixel(x, y, color)
            x++
        }
    }

    private fun drawAxes(g: Graphics) {
        val prefixText = "U+%04X${Text.X_CROSS}${Text.X_CROSS}".format(prefix)
        Text.drawText(g, 8, 15, prefixText)

        for(i in 0..15) { // vertical axis
            val text =
                    if(this.flip)
                        "%X${Text.X_CROSS}".format(i) // 0x - Fx
                    else
                        "${Text.X_CROSS}%X".format(i) // x0 - xF
            var y = Guides.gridStart.yi + Guides.gridSize.yi/2 - 4
            val x = Guides.gridStart.xi - 3 - 8
            y += (Guides.gridSize.xi-1) * i
            Text.drawText(g, x, y, text, if(this.flip) 0 else -1)
        }

        for(i in 0..15) { // horizontal axis
            val text =
                    if(this.flip)
                        "${Text.X_CROSS}%X".format(i) // x0 - xF
                    else
                        "%X${Text.X_CROSS}".format(i) // 0x - Fx
            val y = Guides.gridStart.yi - 2 - 8
            var x = Guides.gridStart.xi + Guides.gridSize.xi/2 - 4
            x += (Guides.gridSize.xi-1) * i
            Text.drawText(g, x, y, text, if(this.flip) -1 else 0)
        }
    }

    private fun drawGuides(g: Graphics) {
        val guide = Images["glyph_guide_box"]

        for(xIndex in 0 until 16) {
            for(yIndex in 0 until 16) {
                val pos = Guides.gridStart + (Guides.gridSize - vec(1, 1)) * vec(xIndex, yIndex)
                g.drawImage(guide, pos.xi, pos.yi, null)
            }
        }
    }

    private fun drawGlyphs(g: Graphics, glyphs: HexFile) {
        for(xIndex in 0 until 16) {
            for(yIndex in 0 until 16) {
                val codepoint = (prefix shl 8) or if(flip)
                    yIndex shl 4 or xIndex
                else
                    xIndex shl 4 or yIndex
                val glyph = glyphs.glyphs[codepoint] ?: continue
                if(glyph.missing) continue
                val gridPos = Guides.gridStart + (Guides.gridSize - vec(1, 1)) * vec(xIndex, yIndex)
                val glyphX = 2
                val glyphY = 2

                g.drawImage(glyph.image, gridPos.xi + glyphX + 1, gridPos.yi + glyphY + 1, null)
            }
        }
    }

    private fun drawReferenceGlyphs(g: Graphics) {
        val target = referenceScale * 5
        var size: Float = target.toFloat()
        var nextJump = size/2
        var font = Font("SansSerif", Font.PLAIN, 10).deriveFont(Font.PLAIN, size)
        for(i in 0..10) {
            val ascent = font.createGlyphVector((g as Graphics2D).fontRenderContext,"X").visualBounds.height
            if(ascent == target.toDouble()) {
                break
            } else {
                if(ascent > target) {
                    size -= nextJump
                } else {
                    size += nextJump
                }
                nextJump /= 2
                font = font.deriveFont(Font.PLAIN, size)
            }
        }

        g.font = font
        (g as Graphics2D).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.color = Color.BLACK
        for(xIndex in 0 until 16) {
            for(yIndex in 0 until 16) {
                val codepoint = (prefix shl 8) or if(flip)
                    yIndex shl 4 or xIndex
                else
                    xIndex shl 4 or yIndex
                val gridPos = Guides.gridStart + (Guides.gridSize - vec(1, 1)) * vec(xIndex, yIndex)

                g.drawString(String(Character.toChars(codepoint)),
                        (gridPos.xi + 2)*referenceScale,
                        (gridPos.yi + 2 + 7)*referenceScale
                )
            }
        }
    }
}
