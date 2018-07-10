package co.thecodewarrior.unifontcli.common

import java.awt.Graphics
import kotlin.math.min

object Text {
    val X_CROSS = '\u00FE' // \u00FE is replaced with a custom character which is a small x with perpendicular lines
    val font = Images["font"]
    val miniFont = Images["font_mini"]

    fun drawText(g: Graphics, x: Int, y: Int, text: String, tracking: Int = 0) {
        var currentX = x
        text.forEach {
            drawChar(g, currentX, y, it)
            currentX += 8 + tracking
        }
    }

    fun drawChar(g: Graphics, x: Int, y: Int, char: Char) {
        val glyphNumber = min(char.toInt(), 255)
        val glyphX = (glyphNumber and 0x0F) * 16
        val glyphY = (glyphNumber shr 4 and 0x0F) * 16
        g.drawImage(font, x, y, x+8, y+16, glyphX, glyphY, glyphX+8, glyphY+16, null)
    }

    fun drawMiniText(g: Graphics, x: Int, y: Int, text: String, tracking: Int = 0) {
        var currentX = x
        text.forEach {
            drawMiniChar(g, currentX, y, it)
            currentX += 6 + tracking
        }
    }

    fun drawMiniChar(g: Graphics, x: Int, y: Int, char: Char) {
        val glyphNumber = min(char.toInt(), 255)
        val glyphX = (glyphNumber and 0x0F) * 6
        val glyphY = (glyphNumber shr 4 and 0x0F) * 7
        g.drawImage(font, x, y, x+6, y+7, glyphX, glyphY, glyphX+6, glyphY+7, null)
    }
}