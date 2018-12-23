package co.thecodewarrior.unifontgui.typesetting

import co.thecodewarrior.unifontgui.utils.Pos
import co.thecodewarrior.unifontlib.Glyph
import co.thecodewarrior.unifontlib.Unifont
import co.thecodewarrior.unifontlib.utils.Color
import kotlin.streams.asSequence

class Typesetter(val project: Unifont) {

    fun typeset(text: String): List<TextRun> {
        var run = TextRun()
        val runs = mutableListOf<TextRun>()

        @Suppress("UNCHECKED_CAST")
        val glyphs = text.codePoints().asSequence()
            .mapIndexed { i, codepoint -> i to project[codepoint] }
            .filter { (_, glyph) -> glyph != null }
            .toMutableList() as MutableList<Pair<Int, Glyph>>

        var cursor = Pos(0, 0)
        for((i, glyph) in glyphs) {
            if("A${glyph.character}A".lines().size > 1) {
                runs.add(run)
                run = TextRun()
                cursor = Pos(0, cursor.y + project.settings.size + 1)
            }
            val glyphPos = Pos(
                cursor.x + glyph.leftBearing,
                cursor.y - (project.settings.size - project.settings.baseline)
            )
            val placed = PlacedGlyph(glyph, glyphPos, i)
            run.glyphs.add(placed)
            cursor = Pos(cursor.x + glyph.advance, cursor.y)
        }
        runs.add(run)
        return runs
    }
}

class TextRun {
    val glyphs = mutableListOf<PlacedGlyph>()
}

data class PlacedGlyph(val glyph: Glyph, val pos: Pos, val index: Int)
