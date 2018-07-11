package co.thecodewarrior.unifontcli.utils

import java.awt.image.BufferedImage
import kotlin.math.min

private class BufferedImagePixelIterator(val image: BufferedImage, val minX: Int, val minY: Int, val maxX: Int, val maxY: Int): Iterator<Pixel> {

    private val buffer = IntArray(100)
    private var bufferIndex = 0
    private var bufferLength = 0
    private var currentX = minX
    private var currentY = minY

    private fun refillBuffer() {
        val widthToRead = min(maxX+1-currentX, buffer.size)
        image.getRGB(currentX, currentY, widthToRead, 1, buffer, 0, 0 /* 1 row = no scan width */)
        bufferIndex = 0
        bufferLength = widthToRead
    }

    override fun hasNext(): Boolean {
        return currentY <= maxY
    }

    override fun next(): Pixel {
        if(currentY > maxY) {
            throw IllegalStateException("Iterator already complete")
        }

        bufferIndex++
        if(bufferIndex >= bufferLength)
            refillBuffer()

        val pixel = Pixel(currentX, currentY, buffer[bufferIndex])

        currentX++
        if(currentX > maxX) {
            currentX = minX
            currentY++
        }

        return pixel
    }

}

private class BufferedImagePixelSequence(val image: BufferedImage, val minX: Int, val minY: Int, val maxX: Int, val maxY: Int): Sequence<Pixel> {
    override fun iterator(): Iterator<Pixel> {
        return BufferedImagePixelIterator(image, minX, minY, maxX, maxY)
    }
}

data class Pixel(val x: Int, val y: Int, val color: Int)

fun BufferedImage.pixels(x: Int = 0, y: Int = 0, width: Int = this.width, height: Int = this.height): Sequence<Pixel> {
    if(width <= 0)
        throw IllegalArgumentException("Width must be greater than zero.")
    if(height <= 0)
        throw IllegalArgumentException("Height must be greater than zero.")
    return BufferedImagePixelSequence(this, x, y, x+width-1, x+height-1)
}
