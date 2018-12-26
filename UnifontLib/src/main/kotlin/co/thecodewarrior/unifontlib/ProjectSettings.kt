package co.thecodewarrior.unifontlib

import java.awt.Color

/**
 * ```
 * {
 *     "size": <int>,
 *     "baseline": <int>,
 *     "capHeight": <int>,
 *     "xHeight": <int>,
 *     "descender": <int>,
 *     "defaultBearing": <int>,
 *     "autoKernParameters": {
 *         "spacing": <int>,
 *         "fillThreshold": <int>,
 *         "depthLimit": <int>,
 *     },
 *     "verticalGuides": [
 *         {
 *             "position": <int>,
 *             "color": <color>
 *         }
 *     ],
 *     "horizontalGuides": [
 *         { "position": <int>, "color": <color> }
 *     ]
 * }
 * ```
 */
data class ProjectSettings(
    /**
     * width/height of glyph images
     */
    val size: Int,
    /**
     * the height of the baseline from the bottom of the glyph
     */
    val baseline: Int,
    /**
     * the cap height of the baseline from the baseline
     */
    val capHeight: Int,
    /**
     * the x height of the font from the baseline
     */
    val xHeight: Int,
    /**
     * the depth of the descender from the baseline
     */
    val descender: Int,
    /**
     * The default bearing value for new glyphs
     */
    val defaultBearing: Int,
    /**
     * The parameters for the auto-kerning algorithm
     */
    val autoKernParameters: AutoKernParameters,
    /**
     * A list of vertical guidelines to display in the glyph editor
     */
    val verticalGuides: List<EditorGuide>,
    /**
     * A list of horizontal guidelines to display in the glyph editor
     */
    val horizontalGuides: List<EditorGuide>
)

data class AutoKernParameters(
    /**
     * the target distance between glyphs when auto-kerning
     */
    val spacing: Int,
    /**
     * any vertical gaps of this size or smaller will be filled in to prevent letters slipping inside each other
     */
    val fillThreshold: Int,
    /**
     * The maximum allowed difference between the extremes of a profile. (e.g. the stem and crossbar of the `T` glyph)
     * Any differences greater than this will be clamped.
     */
    val depthLimit: Int
)

data class EditorGuide(
    /**
     * position of guide between pixels, `0` being on the minX/Y edge and the font image size being on the maxX/Y edge
     */
    var position: Int,
    /**
     * color of the guide. Valid formats are: "#RGB", "#ARGB", "#RRGGBB", "#AARRGGBB"
     */
    val color: Color
)