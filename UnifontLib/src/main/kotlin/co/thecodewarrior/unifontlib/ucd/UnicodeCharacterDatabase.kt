package co.thecodewarrior.unifontlib.ucd

import java.nio.file.Path
import java.util.*

class UnicodeCharacterDatabase(val path: Path) {
    val blocks by lazy {
        val file = UCDFile(path.resolve("Blocks.txt"))
        file.load()

        return@lazy file.data.mapValues { it.value[0] }
    }

    val codepoints by lazy {
        val file = UCDFile(path.resolve("Blocks.txt"))
        file.load()

        return@lazy TreeMap(file.data.map { (key, value) ->
            UCDCodepoint.create(key.start, value)
        }.associateBy { it.codepoint })
        // not using associateByTo because TreeMap inserts are expensive,
        // I assume building the tree as one chunk has been optimized internally
    }
}

class UCDFile(val path: Path) {
    val data = mutableMapOf<IntRange, List<String>>()

    fun load() {
        val lines = path.toFile().readLines()
        lines.forEach { fullLine ->
            val line = fullLine.split('#')[0]
            if(line.isBlank())
                return@forEach
            val columns = line.split(';').map { it.trim() }
            val codepoints = columns[0].split("..").map { it.toInt(16) }
            val codepointRange = codepoints[0]..codepoints.getOrElse(1, { codepoints[0] })
            data[codepointRange] = columns.drop(1)
        }
    }
}

data class UCDCodepoint(val codepoint: Int, val name: String) {
    companion object {
        fun create(codepoint: Int, data: List<String>): UCDCodepoint {
            return UCDCodepoint(codepoint, data[0])
        }
    }
}