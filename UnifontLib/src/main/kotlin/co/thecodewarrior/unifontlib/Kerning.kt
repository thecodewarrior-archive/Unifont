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

    private val _profiles = mutableListOf<KernProfile>()
    val profiles: MutableList<KernProfile>
        get() {
            if(!loaded) load()
            return _profiles
        }

    private val _pairs = mutableMapOf<KernPair, Int>()
    val pairs: MutableMap<KernPair, Int>
        get() {
            if(!loaded) load()
            return _pairs
        }

    private val glyphsRegex = """([0-9a-fA-F]+)-([0-9a-fA-F]+): (-?[0-9]+)""".toRegex()
    private val classRegex = """([A-Za-z_]+)-([A-Za-z_]+): (-?[0-9]+)""".toRegex()
    private val profileRegex = """([0-9]+)-([0-9]+): (-?[0-9]+)""".toRegex()

    fun markDirty() {
        isDirty = true
    }

    fun load() {
        try {
            loaded = true
            if(!Files.exists(path)) return
            lines@ for(it in Files.lines(path)) {
                if(it.isBlank()) continue
                val line = it.trim()

                val match: MatchResult?
                val pair: KernPair
                when(line[0]) {
                    'G' -> {
                        match = glyphsRegex.matchEntire(line.drop(2)) ?: continue@lines
                        val (left, right) = match.destructured
                        pair = KernPair.Glyphs(left.toInt(16), right.toInt(16))
                    }
                    'C' -> {
                        match = classRegex.matchEntire(line.drop(2)) ?: continue@lines
                        val (left, right) = match.destructured
                        pair = KernPair.Classes(left, right)
                    }
                    'P' -> {
                        match = profileRegex.matchEntire(line.drop(2)) ?: continue@lines
                        val (left, right) = match.destructured
                        pair = KernPair.Profiles(left.toInt(), right.toInt())
                    }
                    else -> continue@lines
                }
                val (_, _, kern) = match.destructured
                pairs[pair] = kern.toInt()
            }
        } catch (e: Exception) {
            throw HexException("Error loading kerning from $path", e)
        }
    }

    fun save() {
        try {
            OutputStreamWriter(Files.newOutputStream(path)).use { output ->
                pairs.entries.sortedBy { it.value }.forEach { (pair, kern) ->
                    when(pair) {
                        is KernPair.Glyphs -> output.write("G:%04X-%04X: %d\n".format(pair.left, pair.right, kern))
                        is KernPair.Classes -> output.write("C:%s-%s: %d\n".format(pair.left, pair.right, kern))
                        is KernPair.Profiles -> output.write("P:%d-%d: %d\n".format(pair.left, pair.right, kern))
                    }
                }
            }
            this.isDirty = false
        } catch (e: Exception) {
            throw HexException("Error saving kerning in $path", e)
        }
    }
}

sealed class KernPair: Comparable<KernPair> {
    data class Glyphs(val left: Int, val right: Int): KernPair() {
        override fun compareTo(other: KernPair): Int {
            if(other !is Glyphs) return -1
            var result = other.left - left
            if (result == 0) result = other.right - right
            return result
        }
    }

    data class Classes(val left: String, val right: String): KernPair() {
        override fun compareTo(other: KernPair): Int {
            when(other) {
                is Glyphs -> return 1
                is Profiles -> return -1
                is Classes -> {
                    var result = left.compareTo(other.left)
                    if (result == 0) result = right.compareTo(other.right)
                    return result
                }
            }
        }
    }

    class Profiles(val left: Int, val right: Int): KernPair() {
        override fun compareTo(other: KernPair): Int {
            if(other !is Profiles) return 1
            var result = other.left - left
            if (result == 0) result = other.right - right
            return result
        }

        override fun hashCode(): Int {
            return left.hashCode() xor right.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Profiles

            return (left == other.left && right == other.right) || (left == other.right || right == other.left)
        }
    }
}

fun Unifont.autoKern(targetSpacing: Int, gapSize: Int) {
    val glyphs = this.files.asSequence()
        .flatMap { it.glyphs.values.asSequence() }
        .filter { !it.missing && !it.noAutoKern }
        .map {
            it.leftProfile = -1
            it.rightProfile = -1
            it to GlyphProfile(it, gapSize)
        }.toList()

    var i = 0
    val profileMap = mutableMapOf<KernProfile, Int>()
    glyphs.asSequence()
        .forEach { (glyph, profile) ->
            val leftIndex = profileMap.getOrPut(profile.leftProfile) { i++ }
            val rightIndex = profileMap.getOrPut(profile.rightProfile) { i++ }
            profile.leftProfile.index = leftIndex
            profile.rightProfile.index = rightIndex
            glyph.leftProfile = leftIndex
            glyph.rightProfile = rightIndex
            glyph.markDirty()
        }

    val profiles = profileMap.entries.sortedBy { it.value }.map { it.key }

    val pairs = profiles.asSequence()
            .flatMap { leftProfile ->
                profiles.asSequence()
                        .drop(leftProfile.index)
                        .map { leftProfile to it }
            }

    kerning.profiles.clear()
    kerning.profiles.addAll(profiles)
    kerning.pairs.keys.filter { it is KernPair.Profiles }.forEach {
        kerning.pairs.remove(it)
    }

    for(pair in pairs) {
        val kernPair = KernPair.Profiles(pair.first.index, pair.second.index)

        val gaps = pair.first + pair.second
        val minGap = gaps.minDistance
        if(minGap != null && minGap != Int.MAX_VALUE) {
            val kernAmount = targetSpacing - minGap
            if(kernAmount != 0) kerning.pairs[kernPair] = kernAmount
            continue
        }
    }
    kerning.markDirty()
}

class GlyphProfile(glyph: Glyph, gapSize: Int) {
    val leftProfile: KernProfile
    val rightProfile: KernProfile

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

        val leftLimit = leftGaps.min()!! + 2
        this.leftProfile = KernProfile(leftGaps.map { min(it, leftLimit) })

        val rightLimit = rightGaps.min()!! + 2
        this.rightProfile = KernProfile(rightGaps.map { min(it, rightLimit) })
    }
}

data class KernProfile(val gaps: List<Int>) {
    var index: Int = -1
    operator fun plus(other: KernProfile): KernGaps {
        return KernGaps(gaps.zip(other.gaps).map { (a, b) -> a + b })
    }
}

data class KernGaps(val gaps: List<Int>) {
    val minDistance = gaps.min()
}
