package co.thecodewarrior.unifontlib

import java.awt.Color

/**
 * ```
 * {
 *     "size": <int>, // width/height of glyph images
 *     "baseline": <int>, // the height of the baseline from the bottom of the glyph
 *     "capHeight": <int>, // the cap height of the baseline from the baseline
 *     "xHeight": <int>, // the x height of the font from the baseline
 *     "descender": <int>, // the depth of the descender from the baseline
 *     "verticalGuides": [
 *         {
 *             "position": <int>, // position of guide between pixels,
 *                                // `0` being on the minX/Y edge
 *                                // and `size` being on the maxX/Y edge
 *             "color": <color> // color of the guide. Valid formats are:
 *                              // "#RGB", "#ARGB", "#RRGGBB", "#AARRGGBB"
 *         }
 *     ],
 *     "horizontalGuides": [
 *         { "position": <int>, "color": <color> }
 *     ]
 * }
 * ```
 */
data class ProjectSettings(
    val size: Int, val baseline: Int, val capHeight: Int, val xHeight: Int, val descender: Int,
    val verticalGuides: List<EditorGuide>, val horizontalGuides: List<EditorGuide>
)


data class EditorGuide(var position: Int, val color: Color)