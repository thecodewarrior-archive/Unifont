package co.thecodewarrior.unifontcli.commands.exporters.ttf

import co.thecodewarrior.unifontcli.commands.exporters.Exporter
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
import me.tongfei.progressbar.ProgressBar
import java.awt.*
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.AffineTransformOp
import java.awt.geom.AffineTransform
import java.awt.Toolkit.getDefaultToolkit

class ExportTTF: Exporter(name="mc") {
    override fun run() {
        unifont.all.loadWithProgress()

        val totalGlyphs = unifont.files.sumBy { it.glyphs.count { !it.value.missing } }.toLong()
        ProgressBar("Draw Glyph", totalGlyphs).use { progress ->
            for(page in 0..255) {
                drawPage(page, progress)
            }
        }
    }

    fun drawPage(page: Int, progress: ProgressBar) {
        val image = BufferedImage(128, 128, BufferedImage.TYPE_BYTE_BINARY)
        val g = image.graphics
        g.color = Color.WHITE
        g.fillRect(0, 0, image.width, image.height)

        val range = page*256 .. (page+1)*256
        unifont.filesForCodepoints(range).forEach { file ->
            file.glyphs.forEach { codepoint, glyph ->
                if(!glyph.missing && codepoint in range) {
                    val xIndex = codepoint and 0x0F
                    val yIndex = codepoint and 0xF0 shr 4

                    val x = 8 * xIndex
                    val y = 8 * yIndex

                    g.drawImage(glyph.image, x, y, null)
                    progress.step()
                }
            }
        }

        ImageIO.write(image, file.extension, filename(page))
    }

    private fun filename(page: Int): File {
        val hex = "%02x".format(page)

        return file
    }
}
