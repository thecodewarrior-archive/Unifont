package co.thecodewarrior.unifontcli.commands.importers

import co.thecodewarrior.unifontcli.common.*
import co.thecodewarrior.unifontcli.utils.hex
import co.thecodewarrior.unifontcli.utils.nullableFlag
import co.thecodewarrior.unifontcli.utils.pixels
import co.thecodewarrior.unifontlib.Glyph
import co.thecodewarrior.unifontlib.utils.isColor
import co.thecodewarrior.unifontlib.utils.max
import co.thecodewarrior.unifontlib.utils.min
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.option
import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class ImportGuides: Importer(
        name="guides"
) {
    val prefix by option("-p", "--prefix", help = "The codepoint prefix in hex. The input image will be interpreted to " +
            "contain the codepoints from U+xxxx00 to U+xxxxFF. This value will be read from the image's metadata bars " +
            "if not specified.").hex()
    val flip by option("-f", "--flip", help = "Flips the glyph order to go left to right, then top to bottom. This value " +
            "will be read from the image's metadata bars if not specified").nullableFlag("-F", "--unflip")

    override fun run() {
        val image = ImageIO.read(this.file)

        // use command line arguments or, if they aren't supplied, read the metadata bars from the image.
        val prefix = prefix ?: readMetadataLine(image, row = 0, bits = 16)
        ?: throw PrintMessage("Prefix metadata bar is corrupt. Please specify explicitly with --prefix")
        val flip = flip ?: readMetadataLine(image, row = 1, bits = 1)?.let { it == 1 }
        ?: throw PrintMessage("Flip metadata bar is corrupt. Please specify explicitly with --flip or --unflip")

        readGlyphs(image, prefix, flip)

        unifont.save()
    }

    private fun readMetadataLine(image: BufferedImage, row: Int, bits: Int): Int? {
        val y = 3 + row * 2
        var x = 3
        var value = 0

        val leftEdgeIntact = image.isColor(x-1, y-1, 1, 3, Color.BLACK)
        val rightEdgeIntact = image.isColor(x+bits, y-1, 1, 3, Color.BLACK)
        val topEdgeIntact = image.isColor(x, y-1, bits, 1, Color.BLACK)
        val bottomEdgeIntact = image.isColor(x, y+1, bits, 1, Color.BLACK)
        if(!(leftEdgeIntact && rightEdgeIntact && topEdgeIntact && bottomEdgeIntact)) {
            return null
        }

        for(i in 0 until bits) {
            val mask = 1 shl (15-i)
            // off bit = white, on bit = black. Not the other way round because white is background, black is foreground
            if(image.isColor(x, y, Color.BLACK)) {
                value = value or mask
            } else if(!image.isColor(x, y, Color.WHITE)) {
                return null
            }
            x++
        }

        return value
    }

    private fun readGlyphs(image: BufferedImage, prefix: Int, flip: Boolean) {
        for(xIndex in 0 until 16) {
            for(yIndex in 0 until 16) {
                val gridPos = Guides.gridStart + (Guides.gridSize - vec(1, 1)) * vec(xIndex, yIndex)
                val glyphImage = readGlyph(image, gridPos)
                val codepoint = (prefix shl 8) or if(flip)
                    yIndex shl 4 or xIndex
                else
                    xIndex shl 4 or yIndex
                val file = unifont.fileForCodepoint(codepoint)
                file.glyphs.getOrPut(codepoint) { Glyph(codepoint, glyphImage) }.image = glyphImage
                file.markDirty()
            }
        }
    }

    private fun readGlyph(image: BufferedImage, gridPos: Vec2d): BufferedImage {
        val gridSubimage = image.getSubimage(gridPos.xi + 1, gridPos.yi + 1,
                Guides.gridSize.xi - 2, Guides.gridSize.yi - 2)
        var left = Int.MAX_VALUE
        var right = Int.MIN_VALUE
        var top = Int.MAX_VALUE
        var bottom = Int.MIN_VALUE

        gridSubimage.pixels().forEach { (x, y, color) ->
            if(color == Color.BLACK.rgb) {
                left = min(left, x)
                right = max(right, x)
                top = min(top, x)
                bottom = max(bottom, x)
            }
        }

        val height = 16

        if(left == Int.MAX_VALUE && right == Int.MIN_VALUE && top == Int.MAX_VALUE && bottom == Int.MIN_VALUE) {
            return BufferedImage(8, height, BufferedImage.TYPE_BYTE_BINARY, Glyph.COLOR_MODEL)
        }

        val width = when {
            left < 8 -> 32
            right < 16 -> 8
            right < 24 -> 16
            right < 32 -> 24
            else -> throw RuntimeException("Somehow the right >= 32 (it is $right). This shouldn't happen.")
        }

        val inGridY = 8
        val inGridX = if(width == 32) 0 else 8

        val glyphImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, Glyph.COLOR_MODEL)

        val glyphSubimage = gridSubimage.getSubimage(inGridX, inGridY, width, height).pixels()
        glyphSubimage.forEach { (x, y, color) ->
            if(color == Color.BLACK.rgb) {
                glyphImage.setRGB(x, y, Color.BLACK.rgb)
            }
        }

        return glyphImage
    }
}
