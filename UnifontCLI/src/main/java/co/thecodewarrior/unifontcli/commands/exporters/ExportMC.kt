package co.thecodewarrior.unifontcli.commands.exporters

import co.thecodewarrior.unifontcli.common.*
import co.thecodewarrior.unifontcli.utils.IndexColorModel
import co.thecodewarrior.unifontcli.utils.drawPixel
import co.thecodewarrior.unifontcli.utils.loadWithProgress
import co.thecodewarrior.unifontlib.Glyph
import co.thecodewarrior.unifontlib.GlyphAttribute
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.AffineTransformOp
import java.awt.geom.AffineTransform
import java.awt.image.IndexColorModel
import java.nio.ByteBuffer
import java.util.TreeMap
import kotlin.math.ceil
import kotlin.math.sqrt
import java.io.FileOutputStream
import java.nio.channels.FileChannel



class ExportMC: Exporter(name="minecraft") {
    val glyphs = TreeMap<Int, Glyph>()
    var gridSize = 0
    lateinit var data: ByteBuffer
    lateinit var image: BufferedImage
    lateinit var g: Graphics2D

    override fun run() {
//        Glyph.colorModel = IndexColorModel(Color(1f, 1f, 1f, 0f), Color.WHITE)
        file.mkdirs()
        unifont.files.loadWithProgress()
        unifont.files.forEach {
            glyphs.putAll(it.glyphs)
        }
        glyphs.filter { it.value.missing }.forEach {
            glyphs.remove(it.key)
        }
        gridSize = ceil(sqrt(glyphs.size.toDouble())).toInt()
        image = BufferedImage(gridSize * 8, gridSize * 8, BufferedImage.TYPE_INT_ARGB)
        data = ByteBuffer.allocate(2 + glyphs.size * (4 + 2))

        data.put(8.toByte())
        data.put(2.toByte())
        g = image.createGraphics()
        glyphs.entries.forEachIndexed { i, entry ->
            draw(i, entry.value)
        }
        g.dispose()

        file.mkdirs()
        val output = BufferedImage(image.width, image.height, BufferedImage.TYPE_BYTE_BINARY,
            IndexColorModel(Color(255, 255, 255, 0), Color.WHITE))
        for(x in 0 until image.width) {
            for(y in 0 until image.height) {
                if(image.getRGB(x, y).toUInt() and 0xFF000000u != 0u) {
                    output.setRGB(x, y, Color.WHITE.rgb)
                }
            }
        }
        ImageIO.write(output, "png", file.resolve("font.png"))
        FileOutputStream(file.resolve("metrics.bin"), false).channel.use {
            data.rewind()
            it.write(data)
        }
    }

    private fun draw(index: Int, glyph: Glyph) {
        val x = (index % gridSize) * 8
        val y = (index / gridSize) * 8

        data.putInt(glyph.codepoint).put(glyph.advance.toByte()).put(glyph.leftHang.toByte())

        g.drawImage(glyph.image, x, y, null)
    }
}
