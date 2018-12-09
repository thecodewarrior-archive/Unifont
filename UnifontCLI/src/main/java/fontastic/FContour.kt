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
import fontastic.FPoint

import java.util.ArrayList

/**
 * Class FContour
 *
 * Stores a contour (list of FPoint).
 *
 */
class FContour {

    internal var points: MutableList<FPoint>

    val pointsArray: Array<FPoint>
        get() {
            val pointsArray = points.toTypedArray()
            return pointsArray
        }

    internal constructor() {
        this.points = ArrayList()
    }

    internal constructor(points: Array<PVector>) {
        this.points = ArrayList()
        for (p in points) {
            this.points.add(FPoint(p))
        }
    }

    internal constructor(points: Array<PVector>, controlpoints1: Array<PVector>, controlpoints2: Array<PVector>) {
        this.points = ArrayList()
        for (i in points.indices) {
            this.points.add(FPoint(points[i], controlpoints1[i], controlpoints2[i]))
        }
    }

    internal constructor(points: Array<FPoint>) {
        this.points = ArrayList()
        for (i in points.indices) {
            this.points.add(points[i])
        }
    }

    fun getPoints(): List<FPoint> {
        return points
    }

    fun setPoints(points: Array<PVector>) {
        this.points = ArrayList()
        for (p in points) {
            this.points.add(FPoint(p))
        }
    }

}
*/
