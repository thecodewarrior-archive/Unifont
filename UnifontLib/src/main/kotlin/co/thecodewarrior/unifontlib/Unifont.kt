package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.Color
import co.thecodewarrior.unifontlib.utils.getContinuousRanges
import co.thecodewarrior.unifontlib.utils.overlaps
import co.thecodewarrior.unifontlib.utils.toHexString
import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import java.awt.Color
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

class Unifont(val path: Path) {
    private val blockHexDir = path.resolve("blocks")
    private val homelessHexFile = path.resolve("homeless.hex")
    private val kerningFile = path.resolve("kerning.hex")
    private val autoKerningFile = path.resolve("autoKerning.hex")
    private val projectFile = path.resolve("project.pixfont")

    val settings: ProjectSettings
    val files = mutableListOf<HexFile>()
    var homeless = HexFile(this, homelessHexFile); private set
    val all: List<HexFile>
        get() = (files + listOf(homeless))
    val kerning: Kerning = Kerning(this, kerningFile)
    val autoKerning: Kerning = Kerning(this, autoKerningFile)

    init {
        settings = klaxon.parse(projectFile.toFile().readText())
            ?: throw IllegalStateException("Couldn't parse project json at '$projectFile'")
    }

    fun createBlock(name: String): HexFile {
        val newFile = HexFile(this, blockHexDir.resolve("$name.hex"))
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

    fun filesForCodepoints(range: IntRange): List<HexFile> {
        return files.filter { it.blockRange.overlaps(range) } + listOf(homeless)
    }

    operator fun get(codepoint: Int): Glyph? {
        return fileForCodepoint(codepoint).glyphs[codepoint]
    }

    operator fun set(codepoint: Int, glyph: Glyph?) {
        val file = fileForCodepoint(codepoint)
        file.markDirty()
        if(glyph == null) {
            file.glyphs.remove(codepoint)
        } else {
            file.glyphs[codepoint] = glyph
        }
    }

    operator fun get(pair: KernPair): Int {
        return kerning.pairs[pair] ?: autoKerning.pairs[pair] ?: 0
    }

    operator fun set(pair: KernPair, kern: Int?) {
        kerning.markDirty()
        if(kern == null) {
            kerning.pairs.remove(pair)
        } else {
            kerning.pairs[pair] = kern
        }
    }

    fun clear() {
        files.clear()
        homeless = HexFile(this, homelessHexFile)
    }

    fun loadHeaders() {
        clear()
        Files.createDirectories(blockHexDir)
        Files.list(blockHexDir).asSequence().forEach {
            if(!it.toString().endsWith(".hex")) return@forEach
            val hex = HexFile(this, it)
            hex.loadHeader()
            files.add(hex)
        }
    }

    fun save() {
        if(!Files.exists(blockHexDir)) Files.createDirectory(blockHexDir)
        redistributeGlyphs()
        files.filter { it.isDirty }.forEach { it.save() }
        if(homeless.isDirty) homeless.save(noheader = true)
        if(kerning.isDirty) kerning.save()
        if(autoKerning.isDirty) autoKerning.save()
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

    companion object {
        val klaxon = Klaxon()
            .converter(object: Converter {
                override fun canConvert(cls: Class<*>)
                    = cls == Color::class.java

                override fun toJson(value: Any): String {
                    value as Color
                    return value.toHexString()
                }

                override fun fromJson(jv: JsonValue): Any {
                    return Color(jv.string!!)
                }

            })
    }
}
