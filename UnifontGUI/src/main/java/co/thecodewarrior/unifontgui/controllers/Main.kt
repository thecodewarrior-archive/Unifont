package co.thecodewarrior.unifontgui.controllers

import co.thecodewarrior.unifontgui.ChangeListener
import co.thecodewarrior.unifontgui.Constants
import co.thecodewarrior.unifontgui.sizeHeightTo
import co.thecodewarrior.unifontgui.utils.openFXML
import co.thecodewarrior.unifontgui.utils.loadIdentity
import co.thecodewarrior.unifontlib.Glyph
import co.thecodewarrior.unifontlib.GlyphAttribute
import co.thecodewarrior.unifontlib.Unifont
import co.thecodewarrior.unifontlib.ucd.UnicodeCharacterDatabase
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.layout.FlowPane
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javafx.stage.Stage
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class Main {
    lateinit var project: Unifont
    lateinit var stage: Stage

    @FXML
    lateinit var root: AnchorPane
    @FXML
    lateinit var header: HBox
    @FXML
    lateinit var flowPane: FlowPane
    @FXML
    lateinit var prefixField: TextField

    var prefix: Int = -1
        set(value) {
            if(field != value) {
                field = value
                cells.forEach {
                    val codepoint = (value shl 8) + it.index
                    val file = project.fileForCodepoint(codepoint)
                    if (!file.loaded) file.load()
                    it.glyph = file.glyphs[codepoint]
                }
            }
        }

    val cells = mutableListOf<GlyphCell>()

    fun setup(stage: Stage, project: Unifont) {
        this.stage = stage
        this.project = project
        cells.addAll((0..255).map {
            GlyphCell(project, it)
        })
        flowPane.children.addAll(cells)

        root.requestFocus()
        prefixField.isFocusTraversable = false
        prefixField.focusedProperty().addListener { _, oldValue, newValue ->
            if(oldValue && !newValue) {
                prefixChanged()
            }
        }

        GlobalScope.launch {
            project.loadHeaders()
            prefix = 0
        }
    }

    @FXML
    private fun prefixChanged() {
        if(prefix == -1) return
        prefix = "0${prefixField.text}".toIntOrNull(16) ?: prefix
        prefixField.text = "%04x".format(prefix)
        root.requestFocus()
    }

    @FXML
    private fun menuOpenFile() {
        Main.open(stage)
    }

    @FXML
    private fun menuSaveFile() {
        project.save()
    }

    @FXML
    private fun menuReloadUnicode() {
        val ucdPath = Files.createTempDirectory("UCD")
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
                val bos = BufferedOutputStream(FileOutputStream(filePath))
                val bytesIn = ByteArray(512)
                var read: Int
                while (zipIn.read(bytesIn).also { read = it } != -1) {
                    bos.write(bytesIn, 0, read)
                }
                bos.close()
            }

            zipIn.closeEntry()
            zipEntry = zipIn.nextEntry
        }
        zipIn.close()
        val ucd = UnicodeCharacterDatabase(ucdPath)

        project.all.forEach {
            it.load()
            it.markDirty()
        }

        ucd.blocks.forEach { block ->
            val file = project.findBlockFile(block.value)
            if(file == null) {
                val lowerName = block.value.toLowerCase().replace("[^a-zA-Z]".toRegex(), "_")
                val newFile = project.createBlock(lowerName)
                newFile.blockRange = block.key
                newFile.blockName = block.value
            } else {
                if(file.blockRange != block.key) {
                    file.blockRange = block.key
                }
            }
        }
        project.redistributeGlyphs()
        project.files.forEach { file ->
            val codepoints = ucd.codepoints.subMap(file.blockRange.start, true, file.blockRange.endInclusive, true).values
            codepoints.forEach { codepoint ->
                file.glyphs.getOrPut(codepoint.codepoint) {
                    Glyph(project, codepoint.codepoint, missing = true)
                }.attributes[GlyphAttribute.NAME] = codepoint.name
            }
        }
        ucdPath.toFile().deleteRecursively()
    }

    @FXML
    fun menuOpenPreview() {
        openFXML<FontTester>("FontTester", "Testing ${project.path.fileName}") { controller, stage ->
            controller.setup(project, stage)
        }
    }

    companion object {
        fun open(parent: Stage) {
            val chooser = FileChooser()
            chooser.extensionFilters.add(FileChooser.ExtensionFilter("PixFont file", "*.pixfont"))
            val file = chooser.showOpenDialog(parent)
            if(file != null) {
                open(Unifont(file.toPath().parent!!))
            }
        }

        fun open(project: Unifont) {
            openFXML<Main>("Main", project.path.fileName.toString()) { controller, stage ->
                controller.setup(stage, project)
            }
        }
    }
}

class GlyphCell(val project: Unifont, val index: Int): Canvas(project.settings.size*3+10.0, project.settings.size*3+5 + 30.0), ChangeListener {
    var glyph: Glyph? = null
        set(value) {
            field?.also { this.unlistenTo(it) }
            field = value
            field?.also { this.listenTo(it) }
            reload()
        }

    private var fullImage: BufferedImage = BufferedImage(this.width.toInt(), this.height.toInt(), BufferedImage.TYPE_INT_ARGB)
    private var g: Graphics2D = fullImage.createGraphics()
    private var font = Constants.notoSansBold.sizeHeightTo("X", 15f)

    init {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.font = font
        this.setOnMouseClicked {
            glyph?.also { glyph ->
                openFXML<GlyphEditor>("GlyphEditor", "") { controller, stage ->
                    controller.setup(stage, project, glyph)
                }
            }
        }
    }

    override fun changeOccured(target: Any) {
        reload()
    }

    private fun reload() {
        g.loadIdentity()
        g.background = Color(255, 255, 255, 0)
        g.clearRect(0, 0, fullImage.width, fullImage.height)

        g.color = Color.BLACK
        g.drawPolyline(
            intArrayOf(0, fullImage.width-1, fullImage.width-1, 0, 0),
            intArrayOf(0, 0, fullImage.height-1, fullImage.height-1, 0),
            5
        )

        g.color = Color.GREEN
        g.drawPolyline(
            intArrayOf(4, fullImage.width-5, fullImage.width-5, 4, 4),
            intArrayOf(29, 29, fullImage.height-5, fullImage.height-5, 29),
            5
        )

        glyph?.also { glyph ->
            g.color = Color.BLACK
            val width = font.createGlyphVector(g.fontRenderContext, glyph.character).logicalBounds.width.toFloat()
            g.drawString(glyph.character, (this.width.toFloat()-width)/2, 23f)

            g.translate(5, 30)
            g.scale(3.0, 3.0)
            g.drawImage(glyph.image, 0, 0, null)
        }
        val gc = this.graphicsContext2D
        gc.clearRect(0.0, 0.0, width, height)
        gc.drawImage(SwingFXUtils.toFXImage(fullImage, null), 0.0, 0.0)
    }
}
