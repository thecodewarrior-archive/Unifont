package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.codepointRange
import co.thecodewarrior.unifontlib.utils.getContinuousRanges
import co.thecodewarrior.unifontlib.utils.until
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.streams.asSequence

class HexFile(val path: Path, val legacy: Boolean = false) {
    var isDirty = false
        private set
    val glyphs = TreeMap<Int, Glyph>()
    val missing = TreeSet<IntRange>(Comparator { a, b -> a.start.compareTo(b.start) })
    var blockName: String = ""
    var blockRange: IntRange = IntRange.EMPTY

    fun loadHeader() {
        if(legacy) {
            blockName = "Legacy"
            blockRange = IntRange.EMPTY
            return
        }

        val lines = Files.lines(path).limit(2).asSequence().toList()
        if(lines.size < 2)
            throw InvalidHexException("File has fewer than two lines, valid headers are two lines long.")
        val nameLine = lines[0]
        val rangeLine = lines[1]

        if(nameLine == null || rangeLine == null ||
                nameLine.startsWith("; Block: ") || rangeLine.startsWith("; Range: "))
            throw IllegalArgumentException("File does not contain a valid header. One of the first two lines " +
                    "starts with a character other than `;`")

        blockName = nameLine.removePrefix("; Block: ")
        blockRange = rangeLine.removePrefix("; Range: ").codepointRange()
    }

    fun load() {
        missing.clear()
        Files.lines(path).asSequence().forEach {
            val line = it.trim()
            if(line.isEmpty()) return@forEach

            when {
                line.startsWith("; Block: ") -> {}
                line.startsWith("; Range: ") -> {}
                line.startsWith("; Missing: ") -> missing.add(line.removePrefix("; Missing: ").codepointRange())
                line.startsWith("; Unassigned: ") -> {}
                else -> {
                    val glyph = Glyph.read(line)
                    glyphs[glyph.codepoint] = glyph
                }
            }
        }
    }

    fun save() {
        OutputStreamWriter(Files.newOutputStream(path)).use { output ->
            output.write("; Block: $blockName\n")
            output.write("; Range: ${blockRange.codepointRange()}\n")

            val lines = TreeMap<Int, String>(glyphs.mapValues { it.value.write() })
            lines[Int.MIN_VALUE] = ""

            val unassigned = IntRanges()
            unassigned.add(blockRange)
            unassigned.removeAll(missing)
            unassigned.removeAll(glyphs.keys.getContinuousRanges())

            val ranges = mutableListOf<Pair<IntRange, String>>()
            ranges.addAll(missing.asSequence().map {
                it to "; Missing: ${it.codepointRange()}"
            })
            val sortedRanges = ranges.sortedBy { it.first.start }

            sortedRanges.forEach { (range, line) ->
                val before = lines.floorKey(range.start) ?: return@forEach
                lines[before] = "${lines[before]}\n$line"
            }

            lines.forEach { (_, line) ->
                if(legacy) {
                    val legacyLine = line.until(';')
                    if (legacyLine.isNotBlank())
                        output.write(legacyLine)
                } else {
                    if (line.isNotBlank())
                        output.write(line.removePrefix("\n"))
                }
            }
        }
    }

    fun markDirty() {
        isDirty = true
    }
}

class InvalidHexException(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean):
        RuntimeException(message, cause, enableSuppression, writableStackTrace) {
    constructor(): this(null, null, true, true)
    constructor(message: String): this(message, null, true, true)
    constructor(message: String, cause: Throwable): this(message, cause, true, true)
    constructor(cause: Throwable): this(cause.toString(), cause, true, true)
}
