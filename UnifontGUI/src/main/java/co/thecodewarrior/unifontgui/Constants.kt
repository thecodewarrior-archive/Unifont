package co.thecodewarrior.unifontgui

import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage

object Constants {
    val notoSans = Font.createFont(Font.TRUETYPE_FONT, this::class.java.getResourceAsStream("NotoSans-Regular.ttf"))
    val notoSansBold = Font.createFont(Font.TRUETYPE_FONT, this::class.java.getResourceAsStream("NotoSans-SemiBold.ttf"))
    val notoSansBlack = Font.createFont(Font.TRUETYPE_FONT, this::class.java.getResourceAsStream("NotoSans-SemiCondensedBlack.ttf"))

    fun resource(name: String) = javaClass.getResource(name)
}


fun Font.sizeHeightTo(text: String, target: Float): Font {
    val image = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()
    return sizeToBy(target) { font ->
        font.createGlyphVector(g.fontRenderContext, text).visualBounds.height.toFloat()
    }
}

fun Font.sizeToBy(target: Float, getMetric: (font: Font) -> Float): Font {

    var size: Float = target
    var nextJump = size/2
    var font = this.deriveFont(size)
    for(i in 0..10) {
        val metric = getMetric(font)
        if(metric == target) {
            break
        } else {
            if(metric > target) {
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
