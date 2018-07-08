package co.thecodewarrior.unifontlib

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

class Unifont(val path: Path) {
    private val blockHexDir = path.resolve("blocks")
    private val homelessHexFile = path.resolve("homeless.hex")

    val blocks = mutableListOf<HexFile>()
    var homeless = HexFile(homelessHexFile); private set


    fun createBlock(name: String): HexFile {
        val newFile = HexFile(blockHexDir.resolve("$name.hex"))
        newFile.markDirty()
        blocks.add(newFile)
        return newFile
    }

    fun findBlockFile(name: String): HexFile? {
        return blocks.find { it.blockName == name }
    }

    fun clear() {
        blocks.clear()
        homeless = HexFile(homelessHexFile)
    }

    fun load() {
        clear()
        Files.list(blockHexDir).asSequence().forEach {
            val hex = HexFile(it)
            hex.loadHeader()
            blocks.add(hex)
        }
        homeless.loadHeader()
    }

    fun save() {
        blocks.filter { it.isDirty }.forEach { it.save() }
    }
}