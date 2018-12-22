package co.thecodewarrior.unifontgui.controllers

import co.thecodewarrior.unifontgui.ChangeListener
import co.thecodewarrior.unifontgui.typesetting.TextRun
import co.thecodewarrior.unifontgui.typesetting.Typesetter
import co.thecodewarrior.unifontgui.utils.CanvasWrapper
import co.thecodewarrior.unifontgui.utils.loadIdentity
import co.thecodewarrior.unifontgui.utils.strokeWidth
import co.thecodewarrior.unifontlib.Unifont
import co.thecodewarrior.unifontlib.utils.Color
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.control.Slider
import javafx.scene.control.TextArea
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.awt.Color
import java.awt.Graphics2D
import kotlin.math.roundToInt

class FontTester: ChangeListener {
    lateinit var project: Unifont
    lateinit var typesetter: Typesetter
    lateinit var canvas: CanvasWrapper
    lateinit var stage: Stage

    val g: Graphics2D
        get() = canvas.g

    var runs = listOf<TextRun>()
    var scale = 4.0

    @FXML
    lateinit var lowerPane: AnchorPane
    @FXML
    lateinit var jfxCanvas: Canvas
    @FXML
    lateinit var textArea: TextArea
    @FXML
    lateinit var scaleSlider: Slider

    @FXML
    fun initialize() {
        canvas = CanvasWrapper(jfxCanvas)
        textArea.textProperty().addListener { _, _, _ ->
            reflow()
        }
        jfxCanvas.width = lowerPane.width
        jfxCanvas.height = lowerPane.height
        lowerPane.widthProperty().addListener { _ ->
            jfxCanvas.width = lowerPane.width
            redraw()
        }
        lowerPane.heightProperty().addListener { _ ->
            jfxCanvas.height = lowerPane.height
            redraw()
        }

        textArea.selectionProperty().addListener { _ ->
            redraw()
        }

        scale = scaleSlider.value
        var lastValue = scale.toInt()
        scaleSlider.valueProperty().addListener { _, _, value ->
            val intValue = value.toDouble().roundToInt()
            scaleSlider.value = intValue.toDouble()
            if(intValue != lastValue) {
                lastValue = intValue
                scale = intValue.toDouble()
                redraw()
            }
        }
    }

    fun setup(project: Unifont, stage: Stage) {
        this.project = project
        this.stage = stage
        this.typesetter = Typesetter(project)

        stage.setOnHiding { shutdown() }
    }

    private fun shutdown() {
        unlistenAll()
    }

    private fun unlistenAll() {
        runs.forEach {
            it.glyphs.forEach {
                this.unlistenTo(it.glyph)
            }
        }
    }

    private fun listenAll() {
        runs.forEach {
            it.glyphs.forEach {
                this.listenTo(it.glyph)
            }
        }
    }

    private fun reflow() {
        unlistenAll()
        runs = typesetter.typeset(textArea.text)
        listenAll()
        redraw()
    }

    override fun changeOccured(target: Any) {
        reflow()
    }

    private fun redraw() {
        val selection = textArea.selection.start until textArea.selection.end
        canvas.redraw {
            runs.forEach { run ->
                run.glyphs.forEach { glyph ->
                    g.loadIdentity()
                    g.scale(scale, scale)
                    g.translate(project.settings.size + glyph.pos.x, project.settings.size + glyph.pos.y)

                    if(glyph.index in selection) {
                        g.scale(1 / scale, 1 / scale)
                        g.strokeWidth = 2f
                        g.color = colors[glyph.index % colors.size]
                        g.fillRect(
                            0, 0,
                            (glyph.glyph.advance * scale).toInt(), (project.settings.size * scale).toInt()
                        )
                        g.scale(scale, scale)
                    }

                    g.drawImage(glyph.glyph.image, 0, 0, null)
                }
            }
        }
    }

    companion object {
        private val colors = listOf(
            Color("#e6194B"),
            Color("#f58231"),
            Color("#ffe119")
        )
    }
}