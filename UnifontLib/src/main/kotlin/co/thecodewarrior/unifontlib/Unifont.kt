package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.getContinuousRanges
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

class Unifont(val path: Path) {
    private val blockHexDir = path.resolve("blocks")
    private val homelessHexFile = path.resolve("homeless.hex")

    val files = mutableListOf<HexFile>()
    var homeless = HexFile(homelessHexFile); private set

    fun createBlock(name: String): HexFile {
        val newFile = HexFile(blockHexDir.resolve("$name.hex"))
        newFile.markDirty()
        files.add(newFile)
        return newFile
    }

    fun findBlockFile(name: String): HexFile? {
        return files.find { it.blockName == name }
    }

    fun fileForCodepoint(codepoint: Int): HexFile {
        return files.find { it.blockRange.contains(codepoint) } ?: homeless
    }

    operator fun get(codepoint: Int): Glyph? {
        return fileForCodepoint(codepoint).glyphs[codepoint]
    }

    operator fun set(codepoint: Int, glyph: Glyph?) {
        if(glyph == null) {
            fileForCodepoint(codepoint).glyphs.remove(codepoint)
        } else {
            fileForCodepoint(codepoint).glyphs[codepoint] = glyph
        }
    }

    fun clear() {
        files.clear()
        homeless = HexFile(homelessHexFile)
    }

    fun loadHeaders() {
        clear()
        Files.list(blockHexDir).asSequence().forEach {
            if(!it.toString().endsWith(".hex")) return@forEach
            val hex = HexFile(it)
            hex.loadHeader()
            files.add(hex)
        }
    }

    fun loadBlocks() {
        files.forEach(HexFile::load)
        homeless.load()
    }

    fun save() {
        if(!Files.exists(blockHexDir)) Files.createDirectory(blockHexDir)
        redistributeGlyphs()
        files.filter { it.isDirty }.forEach { it.save() }
        if(homeless.isDirty) homeless.save(noheader = true)
    }

    fun redistributeGlyphs() {
        files.forEach { removeOrphanedGlyphs(it) }
        files.forEach { relocateHomelessGlyphs(it) }
    }

    private fun removeOrphanedGlyphs(file: HexFile) {
        val orphanedHead = file.glyphs.headMap(file.blockRange.start, false)
        val orphanedTail = file.glyphs.tailMap(file.blockRange.endInclusive, false)
        val orphaned = orphanedHead.values + orphanedTail.values
        if(orphaned.isEmpty()) return

        orphanedHead.clear()
        orphanedTail.clear()
        file.markDirty()
        homeless.glyphs.putAll(orphaned.map { it.codepoint to it })
        homeless.markDirty()

        println("Orphaned codepoints have been moved to homeless.hex")
        println(orphaned.map { it.codepoint }.getContinuousRanges().chunked(4)
                .joinToString("\n") { it.joinToString("\t") })
    }

    private fun relocateHomelessGlyphs(file: HexFile) {
        val toAdoptMap = homeless.glyphs.subMap(
                file.blockRange.start, true,
                file.blockRange.endInclusive, true
        )
        if(toAdoptMap.isEmpty()) return
        val toAdopt = toAdoptMap.values.toList()

        toAdoptMap.clear()
        homeless.markDirty()
        file.glyphs.putAll(toAdopt.map { it.codepoint to it })
        file.markDirty()
    }
}