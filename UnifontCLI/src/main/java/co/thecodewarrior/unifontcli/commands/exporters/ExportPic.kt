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
    val border = 16
    val imageSize = 8*256 + border

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

                    val x = border + 8 * xIndex
                    val y = border + 8 * yIndex

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
            val x = border + i*8
            val y = border - 8

            val hex = "%02X".format(i)

            Text.drawText(g, x, y-5, "${hex[0]}")
            Text.drawText(g, x, y, "${hex[1]}")
            if(i and 0xf == 0xf)
                g.drawLine(x+7, y-8, x+7, y+6)
            else
                g.drawLine(x+7, y, x+7, y+6)
        }

        for(i in 0 until 256) {
            val x = border - 8
            val y = border + i*8

            val hex = "%02X".format(i)

            Text.drawText(g, x-2, y, hex)
            if(i and 0xf == 0xf)
                g.drawLine(x-8, y+7, x+6, y+7)
            else
                g.drawLine(x, y+7, x+6, y+7)
        }

    }
}
