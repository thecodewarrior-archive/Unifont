package co.thecodewarrior.unifontlib

import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.TreeMap
import kotlin.math.min

class Kerning(val project: Unifont, val path: Path) {
    var isDirty = false
        private set
    var loaded = false
        private set

    private val _pairs = TreeMap<KernPair, Int>()
    val pairs: TreeMap<KernPair, Int>
        get() {
            if(!loaded) load()
            return _pairs
        }
    private val lineRegex = """([0-9a-fA-F]+)-([0-9a-fA-F]+): (-?[0-9]+)""".toRegex()

    fun markDirty() {
        isDirty = true
    }

    fun load() {
        try {
            loaded = true
            if(!Files.exists(path)) return
            for(it in Files.lines(path)) {
                val line = it.trim()
                if(line.isEmpty()) continue
                val match = lineRegex.matchEntire(line) ?: continue
                val (left, right, kern) = match.destructured
                pairs[KernPair(left.toInt(16), right.toInt(16))] = kern.toInt()
            }
        } catch (e: Exception) {
            throw HexException("Error loading kerning from $path", e)
        }
    }

    fun save() {
        try {
            OutputStreamWriter(Files.newOutputStream(path)).use { output ->
                pairs.forEach { pair, kern ->
                    output.write("%04X-%04X: %d\n".format(pair.left, pair.right, kern))
                }
            }
            this.isDirty = false
        } catch (e: Exception) {
            throw HexException("Error saving kerning in $path", e)
        }
    }
}

data class KernPair(val left: Int, val right: Int): Comparable<KernPair> {
    override fun compareTo(other: KernPair): Int {
        var result = left - other.left
        if(result == 0) result = right - other.right
        return result
    }
}

fun Unifont.autoKern(targetSpacing: Int, gapSize: Int) {
    val glyphs = this.files.asSequence()
        .flatMap { it.glyphs.values.asSequence() }
        .filter { !it.missing && !it.noAutoKern }
        .map { GlyphProfile(it, gapSize) }
        .toMutableList()

    val pairs = glyphs.asSequence()
        .flatMap { glyph ->
            glyphs.asSequence()
                .filter { it !== glyph }
                .map { GlyphPair(glyph, it) }
        }

    autoKerning.pairs.clear()
    for(pair in pairs) {
        val kernPair = KernPair(pair.left.glyph.codepoint, pair.right.glyph.codepoint)
        val minGap = pair.gaps.min()
        if(minGap != null && minGap != Int.MAX_VALUE) {
            val kerning = targetSpacing - minGap
            if(kerning != 0) autoKerning.pairs[kernPair] = kerning
            continue
        }
        autoKerning.pairs.remove(kernPair)
    }
    autoKerning.markDirty()
}

class GlyphPair(val left: GlyphProfile, val right: GlyphProfile) {
    val gaps = left.rightGaps.zip(right.leftGaps).map {
        if(it.first == Int.MAX_VALUE || it.second == Int.MAX_VALUE)
            Int.MAX_VALUE
        else
            it.first + it.second
    }
}

class GlyphProfile(val glyph: Glyph, gapSize: Int) {
    val leftGaps: List<Int>
    val rightGaps: List<Int>

    init {
        val leftGaps = MutableList(glyph.image.height) { Int.MAX_VALUE }
        val rightGaps = MutableList(glyph.image.height) { Int.MAX_VALUE }

        for(x in 0 until glyph.image.width) {
            val leftGap = x + glyph.leftBearing
            val rightGap = (glyph.advance - glyph.leftBearing) - (x + 1)

            var emptyPixels = 0
            for(y in 0 until glyph.image.height) {
                if(glyph[x, y]) {
                    if(emptyPixels <= gapSize) {
                        (y-emptyPixels until y).forEach {
                            leftGaps[it] = min(leftGap, leftGaps[it])
                            rightGaps[it] = min(rightGap, rightGaps[it])
                        }
                    }
                    leftGaps[y] = min(leftGap, leftGaps[y])
                    rightGaps[y] = min(rightGap, rightGaps[y])
                    emptyPixels = 0
                } else {
                    emptyPixels++
                }
            }
        }

        this.leftGaps = leftGaps
        this.rightGaps = rightGaps
    }
}
