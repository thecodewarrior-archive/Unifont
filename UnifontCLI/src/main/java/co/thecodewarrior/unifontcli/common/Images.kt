package co.thecodewarrior.unifontcli.common

import co.thecodewarrior.unifontcli.utils.resource
import java.awt.image.BufferedImage
import java.io.FileNotFoundException
import java.io.IOException
import javax.imageio.ImageIO

object Images {
    val imageCache = mutableMapOf<String, BufferedImage>()


    operator fun get(name: String): BufferedImage {
        val img: BufferedImage
        val cached = imageCache[name]
        if(cached != null) {
            img = cached
        } else {
            img = try {
                val r = resource("$name.png") ?: throw FileNotFoundException("!/$name.png")
                ImageIO.read(r)
            } catch (e: IOException) {
                val pixel = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
                pixel.setRGB(0, 0, 0xFF00FF and (0xFF shl 24))
                pixel
            }
            imageCache[name] = img
        }
        return img
    }

}