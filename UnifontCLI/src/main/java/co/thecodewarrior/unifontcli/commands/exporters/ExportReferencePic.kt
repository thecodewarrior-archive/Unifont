package co.thecodewarrior.unifontcli.commands.exporters

import co.thecodewarrior.unifontcli.commands.importers.Importer
import co.thecodewarrior.unifontcli.common.Text
import co.thecodewarrior.unifontcli.utils.loadWithProgress
import co.thecodewarrior.unifontlib.HexFile
import com.github.ajalt.clikt.parameters.options.option
import me.tongfei.progressbar.ProgressBar
import java.awt.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class ExportReferencePic: Exporter(
        name = "referencepic"
) {
    val plane by option("-p", "--plane")
    val border = 32
    val imageSize = 32*256 + border

    override fun run() {
        unifont.all.loadWithProgress()

        val image = BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB)
        val g = image.graphics
        g.color = Color.WHITE
        g.fillRect(0, 0, image.width, image.height)

        val target = 4 * 5
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

        ProgressBar("Draw Glyph", 65536).use { progress ->
            unifont.all.forEach { file ->
                file.blockRange.forEach codeLoop@{ codepoint ->
                    if(codepoint !in 0..65536) return@codeLoop
                    val xIndex = codepoint and 0xFF
                    val yIndex = codepoint and 0xFF00 shr 8

                    val x = border + 32 * xIndex
                    val y = border + 32 * yIndex

                    g.drawString(String(Character.toChars(codepoint)),
                            x,
                            y + 6*4
                    )
                    g.color = Color.BLUE
                    if(codepoint - 1 !in file.blockRange) {
//                        g.drawLine(
//                                x, y,
//                                x, y+32
//                        )
                    }
                    if(codepoint - 0xFF !in file.blockRange) {
//                        g.drawLine(
//                                x, y,
//                                x+32, y
//                        )
                    }
                    g.color = Color.BLACK
                    progress.step()
                }
            }
        }

        g.font = g.font.deriveFont(g.font.size2D/2)
        addRulers(g)

        g.dispose()

        ImageIO.write(image, file.extension, file)
    }

    private fun addRulers(g: Graphics) {
        g.color = Color.BLACK
        g.drawLine(border-2, border-2, imageSize-1, border-2)
        g.drawLine(border-2, border-2, border-2, imageSize-1)

        for(i in 0 until 256) {
            val x = border + i*32
            val y = border - 2

            val hex = "%02X".format(i)

            g.drawString(hex, x + 8, y-1)
            if(i and 0xf == 0x0)
                g.drawLine(x, y, x, y-16)
            else
                g.drawLine(x, y, x, y-8)
        }

        for(i in 0 until 256) {
            val x = border - 2
            val y = border + i*32

            val hex = "%02X".format(i)

            g.drawString(hex, x-20, y+22)
            if(i and 0xf == 0x0)
                g.drawLine(x, y, x-16, y)
            else
                g.drawLine(x, y, x-8, y)
        }

    }
}
