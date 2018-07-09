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
    val glyphs = TreeMap<Int, Glyph>()
    var blockName: String = "NO_NAME"
    var blockRange: IntRange = Int.MAX_VALUE..Int.MAX_VALUE

    fun loadHeader() {
        try {
            if (!Files.exists(path)) return
            val lines = Files.lines(path).limit(2).asSequence().toList()
            if (lines.size < 2)
                return
            val nameLine = lines[0]
            val rangeLine = lines[1]

            if (nameLine == null || rangeLine == null ||
                    !nameLine.startsWith("; Block: ") || !rangeLine.startsWith("; Range: "))
                return

            blockName = nameLine.removePrefix("; Block: ")
            blockRange = rangeLine.removePrefix("; Range: ").codepointRange()
        } catch (e: Exception) {
            throw HexException("Error loading header for $path", e)
        }
    }

    fun load() {
        try {
            if(!Files.exists(path)) return
            Files.lines(path).asSequence().forEach {
                val line = it.trim()
                if(line.isEmpty()) return@forEach

                when {
                    line.startsWith("; Block: ") -> {}
                    line.startsWith("; Range: ") -> {}
                    line.startsWith("; Unassigned: ") -> {}
                    else -> {
                        val glyph = Glyph.read(line)
                        glyphs[glyph.codepoint] = glyph
                    }
                }
            }
        } catch (e: Exception) {
            throw HexException("Error loading glyphs for $path", e)
        }
    }

    fun save(legacy: Boolean = false, noheader: Boolean = false) {
        try {
            OutputStreamWriter(Files.newOutputStream(path)).use { output ->
                if(!noheader) {
                    output.write("; Block: $blockName\n")
                    output.write("; Range: ${blockRange.codepointRange()}\n")
                }

                val lines = TreeMap<Int, String>(glyphs.mapValues { it.value.write() })
                lines[Int.MIN_VALUE] = ""

                val unassigned = IntRanges()
                unassigned.add(blockRange)
                unassigned.removeAll(glyphs.keys.getContinuousRanges())

                unassigned.ranges.forEach {
                    val range = it.toIntRange()
                    val line = "; Unassigned: ${range.codepointRange()}"
                    val before = lines.floorKey(range.start) ?: return@forEach
                    lines[before] = "${lines[before]}\n$line"
                }

                lines.forEach { (_, line) ->
                    if(legacy) {
                        val legacyLine = line.until(';')
                        if (legacyLine.isNotBlank())
                            output.write(legacyLine + "\n")
                    } else {
                        if (line.isNotBlank())
                            output.write(line.removePrefix("\n") + "\n")
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

class HexException(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean):
        RuntimeException(message, cause, enableSuppression, writableStackTrace) {
    constructor(): this(null, null, true, true)
    constructor(message: String): this(message, null, true, true)
    constructor(message: String, cause: Throwable): this(message, cause, true, true)
    constructor(cause: Throwable): this(cause.toString(), cause, true, true)
}
