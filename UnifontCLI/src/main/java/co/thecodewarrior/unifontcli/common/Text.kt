package co.thecodewarrior.unifontcli.common

import java.awt.Graphics
import kotlin.math.min

object Text {
    val X_CROSS = '\u00FE' // \u00FE is replaced with a custom character which is a small x with perpendicular lines
    val font = Images["font"]

    fun drawText(g: Graphics, x: Int, y: Int, text: String, tracking: Int = 0) {
        var currentX = x
        text.forEach {
            drawChar(g, currentX, y, it)
            currentX += 5 + tracking
        }
    }

    fun drawChar(g: Graphics, x: Int, y: Int, char: Char) {
        val glyphNumber = min(char.toInt(), 255)
        val glyphX = (glyphNumber and 0x0F) * 8
        val glyphY = (glyphNumber shr 4 and 0x0F) * 8
        g.drawImage(font, x, y, x+6, y+8, glyphX, glyphY, glyphX+6, glyphY+8, null)
    }

    fun drawMiniText(g: Graphics, x: Int, y: Int, text: String, tracking: Int = 0) {
        // nop
    }

    fun drawMiniChar(g: Graphics, x: Int, y: Int, char: Char) {
        // nop
    }
}
