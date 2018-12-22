package co.thecodewarrior.unifontcli.commands

import co.thecodewarrior.unifontcli.utils.loadWithProgress
import co.thecodewarrior.unifontcli.utils.removeEscapedNewlines
import co.thecodewarrior.unifontlib.Glyph
import co.thecodewarrior.unifontlib.GlyphAttribute
import co.thecodewarrior.unifontlib.ucd.UnicodeCharacterDatabase
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.nio.file.Paths
import java.io.*
import java.util.zip.ZipEntry
import java.net.URL
import java.util.zip.ZipInputStream



class ReloadUCD: UnifontCommand(
        name = "reloaducd",
        help = """
            Updates the .hex files to reflect the current Unicode Character Database, \
            located in the `projectDir/UCD` directory.

            To update the UCD, download `https://ftp.unicode.org/Public/<version>/ucd/UCD.zip` \
            into this directory and unzip it in the project directory. To automatically download the latest, pass \
            the --download-latest option.
        """.trimIndent().removeEscapedNewlines()
) {

    val downloadLatest by option("--download-latest", help = "Automatically download the latest UCD before reloading.")
            .flag("--no-download-latest")

    override fun run() {
        val ucdPath = Paths.get("UCD")
        if(downloadLatest) {
            val destDirectory = ucdPath.toFile()
            if (destDirectory.exists()) {
                destDirectory.deleteRecursively()
            }
            destDirectory.mkdir()

            val downloadStream = BufferedInputStream(
                    URL("http://www.unicode.org/Public/UCD/latest/ucd/UCD.zip").openStream()
            )
            val zipIn = ZipInputStream(downloadStream)
            var zipEntry: ZipEntry? = zipIn.nextEntry
            while (zipEntry != null) {
                val filePath = destDirectory.resolve(zipEntry.name)
                if (zipEntry.isDirectory) {
                    filePath.mkdir()
                } else {
                    extractFile(zipIn, filePath)
                }

                zipIn.closeEntry()
                zipEntry = zipIn.nextEntry
            }
            zipIn.close()
        }
        val ucd = UnicodeCharacterDatabase(ucdPath)

        unifont.all.loadWithProgress()
        unifont.all.forEach { it.markDirty() }
        updateBlockData(ucd)
        unifont.redistributeGlyphs()
        updateGlyphData(ucd)

        unifont.save()
    }

    fun extractFile(zipIn: ZipInputStream, filePath: File) {
        val bos = BufferedOutputStream(FileOutputStream(filePath))
        val bytesIn = ByteArray(512)
        var read: Int
        while (zipIn.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
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
                    Glyph(unifont, codepoint.codepoint, missing = true)
                }.attributes[GlyphAttribute.NAME] = codepoint.name
            }
        }
    }

}

