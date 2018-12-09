package fontastic

/*
/**
 * Fontastic
 * A font file writer to create TTF and WOFF (Webfonts).
 * http://code.andreaskoller.com/libraries/fontastic
 *
 * Copyright (C) 2013 Andreas Koller http://andreaskoller.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 * @author      Andreas Koller http://andreaskoller.com
 * @modified    06/19/2013
 * @version     0.4 (4)
 */

import processing.core.PVector
import fontastic.FContour
import fontastic.FPoint

import java.util.ArrayList

/**
 * Class FGlyph
 *
 * Stores a glyph with all its properties.
 *
 */
class FGlyph internal constructor(val glyphChar: Char) {
    private val contours: MutableList<FContour>
    var advanceWidth = 512

    val contoursArray: Array<FContour>
        get() {
            val contoursArray = contours.toTypedArray()
            return contoursArray
        }

    val contourCount: Int
        get() = contours.size

    init {
        this.contours = ArrayList()
    }

    fun addContour() {
        contours.add(FContour())
    }

    fun addContour(points: Array<PVector>) {
        contours.add(FContour(points))
    }

    fun addContour(points: Array<FPoint>) {
        contours.add(FContour(points))
    }

    fun addContour(points: Array<PVector>, controlPoints1: Array<PVector>, controlPoints2: Array<PVector>) {
        contours.add(FContour(points, controlPoints1, controlPoints2))
    }

    fun addContour(contour: FContour) {
        contours.add(contour)
    }

    fun getContours(): List<FContour> {
        return contours
    }

    fun getContour(index: Int): FContour {
        return contours[index]
    }

    fun setContour(index: Int, points: Array<PVector>) {
        contours[index] = FContour(points)
    }

    fun setContour(index: Int, points: Array<FPoint>) {
        contours[index] = FContour(points)
    }

    fun setContour(index: Int, contour: FContour) {
        contours[index] = contour
    }

    fun clearContours() {
        this.contours.clear()
    }

}
*/
