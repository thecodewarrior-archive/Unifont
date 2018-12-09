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


/**
 * Class FPoint extends PVector
 *
 * Stores a point with x and y coordinates and optional PVector controlPoint1 and controlPoint2.
 *
 */
class FPoint : PVector {

    private val bezier: Boolean = false
    var controlPoint1: PVector
    var controlPoint2: PVector

    private var hasControlPoint1: Boolean = false
    private var hasControlPoint2: Boolean = false

    constructor() {}

    constructor(point: PVector) {
        this.x = point.x
        this.y = point.y
        this.hasControlPoint1 = false
        this.hasControlPoint2 = false
    }

    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
        this.hasControlPoint1 = false
        this.hasControlPoint2 = false
    }

    constructor(point: PVector, controlPoint1: PVector, controlPoint2: PVector) {
        this.x = point.x
        this.y = point.y
        this.controlPoint1 = controlPoint1
        this.controlPoint2 = controlPoint2
        this.hasControlPoint1 = true
        this.hasControlPoint2 = true
    }

    fun setControlPoint1(controlPoint1: PVector) {
        this.controlPoint1 = controlPoint1
        this.hasControlPoint1 = true
    }

    fun setControlPoint1(x: Float, y: Float) {
        this.controlPoint1 = PVector(x, y)
        this.hasControlPoint1 = true
    }

    fun setControlPoint2(controlPoint2: PVector) {
        this.controlPoint2 = controlPoint2
        this.hasControlPoint2 = true
    }

    fun setControlPoint2(x: Float, y: Float) {
        this.controlPoint2 = PVector(x, y)
        this.hasControlPoint2 = true
    }

    fun hasControlPoint1(): Boolean {
        return hasControlPoint1
    }

    fun hasControlPoint2(): Boolean {
        return hasControlPoint2
    }

}
*/
