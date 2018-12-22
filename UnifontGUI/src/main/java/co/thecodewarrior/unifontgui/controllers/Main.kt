package co.thecodewarrior.unifontgui.controllers

import co.thecodewarrior.unifontgui.Constants
import co.thecodewarrior.unifontgui.sizeHeightTo
import co.thecodewarrior.unifontlib.Glyph
import co.thecodewarrior.unifontlib.Unifont
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.layout.Border
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Paths
import javafx.scene.Scene
import javafx.stage.StageStyle
import javafx.stage.Stage
import javafx.scene.Parent
import javafx.fxml.FXMLLoader
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import java.awt.event.FocusEvent
import java.awt.event.FocusListener

class Main {
    val project = Unifont(Paths.get(""))

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

    val cells = (0..255).map {
        GlyphCell(project, it)
    }

    @FXML
    fun initialize() {
        flowPane.children.addAll(cells)
        project.loadHeaders()
        root.prefWidth = cells[0].width * 32
        root.prefHeight = cells[0].height * 8 + header.height
        prefix = 0

        root.requestFocus()
        prefixField.isFocusTraversable = false
        prefixField.focusedProperty().addListener { _, oldValue, newValue ->
            if(oldValue && !newValue) {
                prefixChanged()
            }
        }
    }

    @FXML
    private fun prefixChanged() {
        prefix = "0${prefixField.text}".toIntOrNull(16) ?: prefix
        prefixField.text = "%04x".format(prefix)
        root.requestFocus()
    }
}

class GlyphCell(val project: Unifont, val index: Int): Canvas(project.settings.size*3+10.0, project.settings.size*3+5 + 30.0) {
    var glyph: Glyph? = null
        set(value) {
            field = value
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
                val fxmlLoader = FXMLLoader(Constants.resource("GlyphEditor.fxml"))
                val root = fxmlLoader.load<Parent>()
                val controller = fxmlLoader.getController<GlyphEditor>()
                controller.setup(project, glyph)
                val stage = Stage()
                stage.title = "Edit U+%04X (${glyph.character})".format(glyph.codepoint)
                stage.scene = Scene(root)
                stage.show()
            }
        }
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