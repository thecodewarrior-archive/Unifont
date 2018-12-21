package co.thecodewarrior.unifontlib

import co.thecodewarrior.unifontlib.utils.getContinuousRanges
import co.thecodewarrior.unifontlib.utils.overlaps
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
    private val projectFile = path.resolve("project.json")

    val settings: ProjectSettings
    val files = mutableListOf<HexFile>()
    var homeless = HexFile(this, homelessHexFile); private set
    val all: List<HexFile>
        get() = (files + listOf(homeless))

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
        if(glyph == null) {
            fileForCodepoint(codepoint).glyphs.remove(codepoint)
        } else {
            fileForCodepoint(codepoint).glyphs[codepoint] = glyph
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
                    val a = if(value.alpha != 255) "%02x".format(value.alpha) else ""
                    val r = "%02x".format(value.red)
                    val g = "%02x".format(value.green)
                    val b = "%02x".format(value.blue)

                    return "#$a$r$g$b"
                }

                override fun fromJson(jv: JsonValue): Any {
                    val jsonString = jv.string!!
                    val hex = jsonString.removePrefix("#")
                    val a: Int
                    val r: Int
                    val g: Int
                    val b: Int

                    when(hex.length) {
                        3 -> {
                            a = 255
                            r = "${hex[0]}${hex[0]}".toInt(16)
                            g = "${hex[1]}${hex[1]}".toInt(16)
                            b = "${hex[2]}${hex[2]}".toInt(16)
                        }
                        4 -> {
                            a = "${hex[0]}${hex[0]}".toInt(16)
                            r = "${hex[1]}${hex[1]}".toInt(16)
                            g = "${hex[2]}${hex[2]}".toInt(16)
                            b = "${hex[3]}${hex[3]}".toInt(16)
                        }
                        6 -> {
                            a = 255
                            r = hex.substring(0..1).toInt(16)
                            g = hex.substring(2..3).toInt(16)
                            b = hex.substring(4..5).toInt(16)
                        }
                        8 -> {
                            a = hex.substring(0..1).toInt(16)
                            r = hex.substring(2..3).toInt(16)
                            g = hex.substring(4..5).toInt(16)
                            b = hex.substring(6..7).toInt(16)
                        }
                        else ->
                            throw IllegalArgumentException("Hex value '$jsonString' is not valid hex " +
                                "(valid formats are #RGB, #ARGB, #RRGGBB, and #AARRGGBB)")
                    }

                    return Color(r, g, b, a)
                }

            })
    }
}