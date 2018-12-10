package co.thecodewarrior.unifontcli.utils

import co.thecodewarrior.unifontlib.HexFile
import co.thecodewarrior.unifontlib.HexLoadHandler
import me.tongfei.progressbar.ProgressBar

fun HexFile.loadWithProgress() {
    ProgressBar("Load glyphs", count.toLong()).use { progress ->
        val handler = object: HexLoadHandler {
            override fun readGlyphs(count: Int) {
                progress.stepBy(count.toLong())
            }
        }
        this.load(handler)
    }
}

fun List<HexFile>.loadWithProgress() {
    val glyphCount = this.sumBy { it.count }.toLong()
    ProgressBar("Load glyphs", glyphCount).use { progress ->
        val handler = object: HexLoadHandler {
            override fun readGlyphs(count: Int) {
                progress.stepBy(count.toLong())
            }
        }
        this.forEach {
            progress.extraMessage = it.blockName
            it.load(handler)
        }
        progress.stepTo(glyphCount)
    }
}