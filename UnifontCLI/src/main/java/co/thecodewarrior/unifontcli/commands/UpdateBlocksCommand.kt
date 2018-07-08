package co.thecodewarrior.unifontcli.commands

import co.thecodewarrior.unifontcli.utils.removeEscapedNewlines
import co.thecodewarrior.unifontlib.HexFile
import co.thecodewarrior.unifontlib.ucd.UnicodeCharacterDatabase
import co.thecodewarrior.unifontlib.utils.codepointsToRangeStrings
import co.thecodewarrior.unifontlib.utils.codepointsToRanges
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import java.nio.file.Path
import java.nio.file.Paths

class UpdateBlocksCommand: UnifontCommand(
        name = "updateblocks",
        help = """
            Updates the block .hex files to reflect the current Unicode Character Database, \
            located in ./UCD in the project directory.
        """.trimIndent().removeEscapedNewlines()
) {
    val files: List<String> by argument(
            name="files",
            help="""
                Limits updates to specified files. If any of the files aren't in the project \
                a message is printed and they are skipped. Specifying limits disables the \
                creation of new files for missing blocks.
            """.trimIndent().removeEscapedNewlines()
    ).multiple(required = false)

    private lateinit var limitPaths: Set<Path>

    override fun run() {
        val ucd = UnicodeCharacterDatabase(Paths.get("UCD"))
        limitPaths = files.map { Paths.get(it).toAbsolutePath() }.toSet()

        printMissingLimits()
        updateBlockData(ucd)
        updateGlyphData(ucd)

        unifont.save()
    }

    private fun shouldProcess(file: HexFile): Boolean {
        if(limitPaths.isEmpty())
            return true
        if(file.path.toAbsolutePath() in limitPaths)
            return true
        return false
    }

    private fun printMissingLimits() {
        val allFiles = (unifont.blocks + listOf(unifont.homeless))
                .map { it.path.toAbsolutePath() }.toSet()
        limitPaths.forEach { limit ->
            if(limit !in allFiles) {
                println("File $limit is not part of this project, it will not be processed.")
            }
        }
    }

    private fun updateBlockData(ucd: UnicodeCharacterDatabase) {
        ucd.blocks.forEach { block ->
            val file = unifont.findBlockFile(block.value)
            if(file == null) {
                if(limitPaths.isNotEmpty()) return
                val lowerName = block.value.toLowerCase().replace("[^a-zA-Z]".toRegex(), "_")
                val newFile = unifont.createBlock(lowerName)
                newFile.blockRange = block.key
                newFile.blockName = block.value
            } else {
                if(!shouldProcess(file)) return
                if(file.blockRange != block.key) {
                    file.blockRange = block.key
                    file.markDirty()
                }
            }
        }
    }

    private fun updateGlyphData(ucd: UnicodeCharacterDatabase) {
        unifont.blocks.forEach { file ->
            removeOrphanedGlyphs(file)
        }

        unifont.blocks.forEach { file ->
            relocateHomelessGlyphs(file)
        }
    }

    private fun removeOrphanedGlyphs(file: HexFile) {
        if(limitPaths.isNotEmpty()) return

        val orphanedHead = file.glyphs.headMap(file.blockRange.start, false)
        val orphanedTail = file.glyphs.tailMap(file.blockRange.endInclusive, false)
        val orphaned = orphanedHead.values + orphanedTail.values
        if(orphaned.isEmpty()) return

        orphanedHead.clear()
        orphanedTail.clear()
        file.markDirty()
        unifont.homeless.glyphs.putAll(orphaned.map { it.codepoint to it })
        unifont.homeless.markDirty()

        println("Orphaned codepoints have been moved to homeless.hex")
        println(codepointsToRangeStrings(orphaned.map { it.codepoint }).chunked(4)
                .joinToString("\n") { it.joinToString("\t") })
    }

    private fun relocateHomelessGlyphs(file: HexFile) {
        if(limitPaths.isNotEmpty()) return

        val toAdoptMap = unifont.homeless.glyphs.subMap(
                file.blockRange.start, true,
                file.blockRange.endInclusive, true
        )
        if(toAdoptMap.isEmpty()) return
        val toAdopt = toAdoptMap.values.toList()

        toAdoptMap.clear()
        unifont.homeless.markDirty()
        file.glyphs.putAll(toAdopt.map { it.codepoint to it })
        file.markDirty()
    }
}

