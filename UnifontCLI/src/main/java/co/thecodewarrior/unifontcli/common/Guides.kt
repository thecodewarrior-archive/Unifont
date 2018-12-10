package co.thecodewarrior.unifontcli.common

import co.thecodewarrior.unifontcli.utils.IndexColorModel
import java.awt.Color
import java.awt.image.IndexColorModel

object Guides {
    val gridStart = vec(61, 25)
    val gridSize = vec(14, 14)
    val backgroundColor = Color(0xffffff)
    val glyphColor = Color(0x000000)
    val gridColor = Color(0xffbfbf)
    val guideColor = Color(0xc0ffff)
    val metricsColor = Color(0x00ff00)

    val colorModel = IndexColorModel(
        backgroundColor,
        glyphColor,
        gridColor,
        guideColor,
        metricsColor
    )
}

