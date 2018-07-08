package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.codepointsToRangeStrings
import co.thecodewarrior.unifontlib.utils.rangesToStrings
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.streams.asSequence

class HexFile(val path: Path) {
    var isDirty = false
        private set
    val glyphs = TreeMap<Int, Glyph>()
    var blockName: String = ""
    var blockRange: IntRange = -1..-1

    fun loadHeader() {
//        ; [Block Name]
//        ; [start]..[end]
        val lines = Files.lines(path).limit(2).asSequence().map {
            val text = it.trim()
            if(!text.startsWith(';'))
                throw IllegalArgumentException("File does not contain a valid header. One of the first two lines " +
                        "starts with a character other than `;`")
            text.removePrefix(";").trim()
        }.toList()

        blockName = lines[0]
        val bounds = lines[0].split("..", limit=2).map { it.toInt(16) }
        blockRange = bounds[0]..bounds[1]
    }

    fun load() {
        Files.lines(path).asSequence().mapNotNull {
            val line = it.trim()
            if(line.isEmpty()) return@mapNotNull null
            if(line[0] in listOf('#', '*', ';')) return@mapNotNull null

            return@mapNotNull Glyph.read(line.takeWhile { it != '#' })
        }
    }

    fun save() {
        OutputStreamWriter(Files.newOutputStream(path)).use { output ->
            output.write("; $blockName\n")
            output.write("; ${rangesToStrings(listOf(blockRange)) { it.toString(16) }[0]}\n")
            glyphs.values.map {
                output.write(it.write() + "\n")
            }
        }
    }

    fun markDirty() {
        isDirty = true
    }
}
