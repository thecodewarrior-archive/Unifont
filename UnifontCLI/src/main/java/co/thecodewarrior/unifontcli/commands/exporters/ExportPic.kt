package co.thecodewarrior.unifontcli.commands.exporters

import co.thecodewarrior.unifontcli.commands.importers.Importer
import co.thecodewarrior.unifontcli.common.Text
import co.thecodewarrior.unifontcli.utils.loadWithProgress
import co.thecodewarrior.unifontlib.HexFile
import com.github.ajalt.clikt.parameters.options.option
import me.tongfei.progressbar.ProgressBar
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class ExportPic: Exporter(
        name = "pic"
) {
    val plane by option("-p", "--plane")
    val border = 32
    val imageSize = 16*256 + border

    override fun run() {
        unifont.all.loadWithProgress()

        val image = BufferedImage(imageSize, imageSize, BufferedImage.TYPE_BYTE_BINARY)
        val g = image.graphics
        g.color = Color.WHITE
        g.fillRect(0, 0, image.width, image.height)
        addRulers(g)

        val totalGlyphs = unifont.files.sumBy { it.glyphs.count { !it.value.missing } }.toLong()
        ProgressBar("Draw Glyph", totalGlyphs).use { progress ->
            unifont.files.forEach { file ->
                file.glyphs.forEach { codepoint, glyph ->
                    val xIndex = codepoint and 0xFF
                    val yIndex = codepoint and 0xFF00 shr 8

                    val x = border + 16 * xIndex
                    val y = border + 16 * yIndex

                    g.drawImage(glyph.image, x, y, null)
                    progress.step()
                }
            }
        }

        g.dispose()

        ImageIO.write(image, file.extension, file)
    }

    private fun addRulers(g: Graphics) {
        g.color = Color.BLACK
        g.drawLine(0, border-1, border-2, border-1)
        g.drawLine(border-2, border-2, imageSize-1, border-2)
        g.drawLine(border-1, 0, border-1, border-2)
        g.drawLine(border-2, border-2, border-2, imageSize-1)

        for(i in 0 until 256) {
            val x = border + i*16
            val y = border - 16

            val hex = "%02X".format(i)

            Text.drawText(g, x, y-1, hex, tracking = -1)
            if(i and 0xf == 0xf)
                g.drawLine(x+15, y-16, x+15, y+14)
            else
                g.drawLine(x+15, y, x+15, y+14)
        }

        for(i in 0 until 256) {
            val x = border - 16
            val y = border + i*16

            val hex = "%02X".format(i)

            Text.drawText(g, x-2, y, hex)
            if(i and 0xf == 0xf)
                g.drawLine(x-16, y+15, x+14, y+15)
            else
                g.drawLine(x, y+15, x+14, y+15)
        }

    }
}
