package co.thecodewarrior.unifontgui

import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage

object Constants {
    val notoSans = Font.createFont(Font.TRUETYPE_FONT, this::class.java.getResourceAsStream("NotoSans-Regular.ttf"))
}

fun Font.sizeCapHeightTo(target: Float): Font {
    val image = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()

    var size: Float = target
    var nextJump = size/2
    var font = this.deriveFont(size)
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
            font = font.deriveFont(size)
        }
    }

    return font
}