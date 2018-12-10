package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.codepointRange
import co.thecodewarrior.unifontlib.utils.getContinuousRanges
import co.thecodewarrior.unifontlib.utils.toIntRange
import co.thecodewarrior.unifontlib.utils.until
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.streams.asSequence

class HexFile(val path: Path) {
    var isDirty = false
        private set
    var loaded = false
        private set
    private val _glyphs = TreeMap<Int, Glyph>()
    val glyphs: TreeMap<Int, Glyph>
        get() {
            if(!loaded) load()
            return _glyphs
        }
    var blockName: String = "NO_NAME"
    var blockRange: IntRange = Int.MAX_VALUE..Int.MAX_VALUE
    var count: Int = 0

    fun loadHeader() {
        try {
            if (!Files.exists(path)) return
            val lines = Files.lines(path).asSequence().map { it.trim() }.toList()
            if (lines.size < 2)
                return
            val nameLine = lines[0]
            val rangeLine = lines[1]

            if (!nameLine.startsWith("; Block: ") || !rangeLine.startsWith("; Range: "))
                return

            blockName = nameLine.removePrefix("; Block: ")
            val rangeMatch = """([\da-zA-Z]+\.\.[\da-zA-Z]+)(?:\s+\((\d+)\))?""".toRegex()
                    .matchEntire(rangeLine.removePrefix("; Range: "))
                    ?: throw HexException("Illegal range line: `$rangeLine`")
            blockRange = rangeMatch.groupValues[1].codepointRange()
            count = rangeMatch.groupValues[2].toIntOrNull() ?: 0
        } catch (e: Exception) {
            throw HexException("Error loading header for $path", e)
        }
    }

    fun load(loadHandler: HexLoadHandler? = null) {
        try {
            loaded = true
            if(!Files.exists(path)) return
            var i = 0
            Files.lines(path).asSequence().forEach {
                val line = it.trim()
                if(line.isEmpty()) return@forEach

                when {
                    line.startsWith("; Block: ") -> {}
                    line.startsWith("; Range: ") -> {}
                    line.startsWith("; Unassigned: ") -> {}
                    else -> {
                        val glyph = Glyph.read(line)
                        _glyphs[glyph.codepoint] = glyph
                        i++
                        if(i >= 64) {
                            loadHandler?.readGlyphs(i)
                            i = 0
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw HexException("Error loading glyphs for $path", e)
        }
    }

    fun save(legacy: Boolean = false, noheader: Boolean = false, saveHandler: HexSaveHandler? = null) {
        try {
            OutputStreamWriter(Files.newOutputStream(path)).use { output ->
                count = _glyphs.count { !it.value.missing }
                if(!noheader) {
                    output.write("; Block: $blockName\n")
                    output.write("; Range: ${blockRange.codepointRange()} ($count)\n")
                }

                var i = 0
                val lines = TreeMap<Int, String>(_glyphs.mapValues {
                    val g = it.value.write()
                    i++
                    if(i >= 64) {
                        saveHandler?.serializeGlyphs(i)
                        i = 0
                    }
                    g
                })
                lines[Int.MIN_VALUE] = ""

                val unassigned = IntRanges()
                unassigned.add(blockRange)
                unassigned.removeAll(_glyphs.keys.getContinuousRanges())

                unassigned.ranges.forEach {
                    val range = it.toIntRange()
                    val line = "; Unassigned: ${range.codepointRange()}"
                    val before = lines.floorKey(range.start) ?: return@forEach
                    lines[before] = "${lines[before]}\n$line"
                }

                i = 0
                lines.forEach { (_, line) ->
                    if(legacy) {
                        val legacyLine = line.until(';')
                        if (legacyLine.isNotBlank())
                            output.write(legacyLine + "\n")
                    } else {
                        if (line.isNotBlank())
                            output.write(line.removePrefix("\n") + "\n")
                    }

                    i++
                    if(i >= 64) {
                        saveHandler?.writeGlyphs(i)
                        i = 0
                    }
                }
            }
        } catch (e: Exception) {
            throw HexException("Error saving glyphs for $path", e)
        }
    }

    fun markDirty() {
        isDirty = true
    }
}

interface HexLoadHandler {
    fun readGlyphs(count: Int)
}

interface HexSaveHandler {
    fun serializeGlyphs(count: Int)
    fun writeGlyphs(count: Int)
}

class HexException(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean):
        RuntimeException(message, cause, enableSuppression, writableStackTrace) {
    constructor(): this(null, null, true, true)
    constructor(message: String): this(message, null, true, true)
    constructor(message: String, cause: Throwable): this(message, cause, true, true)
    constructor(cause: Throwable): this(cause.toString(), cause, true, true)
}
