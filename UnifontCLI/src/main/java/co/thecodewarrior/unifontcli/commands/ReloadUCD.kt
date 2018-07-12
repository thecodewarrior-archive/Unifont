package co.thecodewarrior.unifontcli.commands

import co.thecodewarrior.unifontcli.utils.loadWithProgress
import co.thecodewarrior.unifontcli.utils.removeEscapedNewlines
import co.thecodewarrior.unifontlib.Glyph
import co.thecodewarrior.unifontlib.GlyphAttribute
import co.thecodewarrior.unifontlib.ucd.UnicodeCharacterDatabase
import java.nio.file.Paths

class ReloadUCD: UnifontCommand(
        name = "reloaducd",
        help = """
            Updates the .hex files to reflect the current Unicode Character Database, \
            located in the `projectDir/UCD` directory.

            To update the UCD, download `https://ftp.unicode.org/Public/<version>/ucd/UCD.zip` \
            into this directory and unzip it in the project directory.
        """.trimIndent().removeEscapedNewlines()
) {
    override fun run() {
        val ucd = UnicodeCharacterDatabase(Paths.get("UCD"))

        unifont.all.loadWithProgress()
        unifont.all.forEach { it.markDirty() }
        updateBlockData(ucd)
        updateGlyphData(ucd)

        unifont.save()
    }

    private fun updateBlockData(ucd: UnicodeCharacterDatabase) {
        ucd.blocks.forEach { block ->
            val file = unifont.findBlockFile(block.value)
            if(file == null) {
                val lowerName = block.value.toLowerCase().replace("[^a-zA-Z]".toRegex(), "_")
                val newFile = unifont.createBlock(lowerName)
                newFile.blockRange = block.key
                newFile.blockName = block.value
            } else {
                if(file.blockRange != block.key) {
                    file.blockRange = block.key
                }
            }
        }
    }

    private fun updateGlyphData(ucd: UnicodeCharacterDatabase) {
        unifont.files.forEach { file ->
            val codepoints = ucd.codepoints.subMap(file.blockRange.start, true, file.blockRange.endInclusive, true).values
            codepoints.forEach { codepoint ->
                file.glyphs.getOrPut(codepoint.codepoint) {
                    Glyph(codepoint.codepoint, missing = true)
                }.attributes[GlyphAttribute.NAME] = codepoint.name
            }
        }
    }

}

