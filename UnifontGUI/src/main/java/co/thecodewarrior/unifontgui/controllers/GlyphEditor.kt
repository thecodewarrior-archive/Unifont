package co.thecodewarrior.unifontgui.controllers

import co.thecodewarrior.unifontgui.Constants
import co.thecodewarrior.unifontgui.sizeHeightTo
import co.thecodewarrior.unifontlib.EditorGuide
import co.thecodewarrior.unifontlib.Glyph
import co.thecodewarrior.unifontlib.Unifont
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.nio.file.Paths
import javafx.scene.control.Slider
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import java.awt.BasicStroke
import java.awt.Font

class GlyphEditor {
    lateinit var project: Unifont
    lateinit var glyph: Glyph
    private val horizontalGuides = mutableListOf<EditorGuide>()
    private val verticalGuides = mutableListOf<EditorGuide>()

    @FXML
    lateinit var canvas: Canvas
    @FXML
    lateinit var metrics: VBox

    private val gc: GraphicsContext
        get() = canvas.graphicsContext2D
    private var fullImage: BufferedImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    private var g: Graphics2D = fullImage.createGraphics()

    var mouseDragType: Boolean? = null

    val glyphPos: Pos = Pos(350, 100)
    val referencePos: Pos = Pos(50, 100)
    val glyphSize: Int = 200
    var pixelSize: Int = 0
    var referenceFont: Font = Constants.notoSans


    fun setup(project: Unifont, glyph: Glyph) {
        this.project = project
        this.glyph = glyph
        this.horizontalGuides.addAll(project.settings.horizontalGuides)
        this.horizontalGuides.addAll(project.settings.verticalGuides)
        this.pixelSize = glyphSize/project.settings.size
        this.referenceFont = referenceFont.sizeHeightTo("X", pixelSize*project.settings.capHeight.toFloat())

        createSlider("Advance", 0, 1, glyph.advance) {
            glyph.advance = it
            redrawCanvas()
        }
        createSlider("Left overhang", 0, 0, glyph.leftHang) {
            glyph.leftHang = it
            redrawCanvas()
        }
        redrawCanvas()
    }

    fun createSlider(name: String, minOffset: Int, maxOffset: Int, initialValue: Int, change: (Int) -> Unit) {
        val label = Label(name)
        label.textAlignment = TextAlignment.CENTER
        label.prefWidth = 200.0
        val slider = Slider(minOffset.toDouble(), project.settings.size.toDouble() + maxOffset, initialValue.toDouble())
        slider.isShowTickMarks = true
        slider.isSnapToTicks = true
        slider.majorTickUnit = 1.0
        slider.minorTickCount = 0
        var lastValue = initialValue
        slider.valueProperty().addListener { _, _, value ->
            val intValue = value.toInt()
            slider.value = intValue.toDouble()
            if(intValue != lastValue) {
                lastValue = intValue
                change(intValue)
            }
        }

        metrics.children.add(label)
        metrics.children.add(slider)
    }

    fun pixelCoords(canvasPos: Pos): Pos {
        return (canvasPos - glyphPos) / pixelSize
    }

    fun redrawCanvas() {
        if(fullImage.width != canvas.width.toInt() || fullImage.height != canvas.height.toInt()) {
            fullImage = BufferedImage(canvas.width.toInt(), canvas.height.toInt(), BufferedImage.TYPE_INT_ARGB)
            g = fullImage.createGraphics()
        }
        val scale = glyphSize / project.settings.size.toDouble()

        g.loadIdentity()
        g.background = Color(255, 255, 255, 0)
        g.clearRect(0, 0, fullImage.width, fullImage.height)
        g.strokeWidth = 1f

        drawGlyphEdges()
        g.loadIdentity()

        drawReferenceGuides()
        g.loadIdentity()

        drawGlyphGuides()
        g.loadIdentity()

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        g.translate(glyphPos.x, glyphPos.y)
        g.scale(scale, scale)
        g.drawImage(glyph.image, 0, 0, null)
        g.scale(1.0/scale, 1.0/scale)
        g.loadIdentity()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT)


        val baselineY = (project.settings.size - project.settings.baseline)*pixelSize
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.font = referenceFont
        g.drawString(glyph.character, referencePos.x, referencePos.y + baselineY)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT)

        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
        gc.drawImage(SwingFXUtils.toFXImage(fullImage, null), 0.0, 0.0)
    }

    fun drawGlyphEdges() {
        g.color = Color.green
        g.drawLine(0, glyphPos.y, fullImage.width, glyphPos.y)
        g.drawLine(0, glyphPos.y + glyphSize, fullImage.width, glyphPos.y + glyphSize)

        g.drawLine(glyphPos.x, 0, glyphPos.x, fullImage.height)
        g.drawLine(glyphPos.x + glyphSize, 0, glyphPos.x + glyphSize, fullImage.height)
        g.drawLine(referencePos.x, 0, referencePos.x, fullImage.height)
        g.drawLine(referencePos.x + glyphSize, 0, referencePos.x + glyphSize, fullImage.height)

        g.color = Color.lightGray
        (1 until project.settings.size).forEach {
            g.drawLine(glyphPos.x, glyphPos.y+it*pixelSize, glyphPos.x+glyphSize, glyphPos.y+it*pixelSize)
            g.drawLine(referencePos.x, referencePos.y+it*pixelSize, referencePos.x+glyphSize, referencePos.y+it*pixelSize)

            g.drawLine(glyphPos.x+it*pixelSize, glyphPos.y, glyphPos.x+it*pixelSize, glyphPos.y+glyphSize)
            g.drawLine(referencePos.x+it*pixelSize, referencePos.y, referencePos.x+it*pixelSize, referencePos.y+glyphSize)
        }
    }

    fun drawReferenceGuides() {
        val baselineY = (project.settings.size - project.settings.baseline)*pixelSize

        g.translate(referencePos.x, referencePos.y + baselineY)

        g.color = Color.BLUE
        g.drawLine(0, 0, glyphSize, 0)
        val capHeight = referenceFont.createGlyphVector(g.fontRenderContext,"X").visualBounds.minY.toInt()
        g.drawLine(0, capHeight, glyphSize, capHeight)
        val xHeight = referenceFont.createGlyphVector(g.fontRenderContext,"x").visualBounds.minY.toInt()
        g.drawLine(0, xHeight, glyphSize, xHeight)
        val descender = referenceFont.createGlyphVector(g.fontRenderContext,"y").visualBounds.maxY.toInt()
        g.drawLine(0, descender, glyphSize, descender)


        val metrics = referenceFont.createGlyphVector(g.fontRenderContext, glyph.character).getGlyphMetrics(0)
        val lsb = metrics.lsb.toInt()
        val rsb = metrics.rsb.toInt()

        g.translate(0, -baselineY)

        val advance = metrics.advance.toInt()
        g.drawLine(advance, 0, advance, glyphSize)

        g.stroke = BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, floatArrayOf(pixelSize/2f, pixelSize/2f), pixelSize/4f)
        if(lsb != 0) {
            g.drawLine(lsb, 0, lsb, glyphSize)
        }
        if(rsb != 0) {
            g.drawLine(advance - rsb, 0, advance - rsb, glyphSize)
        }
        g.stroke = BasicStroke(1f)
    }

    fun drawGlyphGuides() {
        val baselineY = (project.settings.size - project.settings.baseline)*pixelSize
        g.translate(glyphPos.x, glyphPos.y + baselineY)

        g.color = Color.BLUE
        g.drawLine(0, 0, glyphSize, 0)
        val capHeight = project.settings.capHeight*pixelSize
        g.drawLine(0, -capHeight, glyphSize, -capHeight)
        val xHeight = project.settings.xHeight*pixelSize
        g.drawLine(0, -xHeight, glyphSize, -xHeight)
        val descender = project.settings.descender*pixelSize
        g.drawLine(0, descender, glyphSize, descender)

        g.color = Color.GREEN
        g.strokeWidth = 2f
        g.drawLine(glyph.leftHang*pixelSize, 0, (glyph.leftHang + glyph.advance)*pixelSize, 0)
        g.strokeWidth = 1f

        g.translate(0, -baselineY)

        horizontalGuides.forEach {
            g.color = it.color
            g.drawLine(0, it.position*pixelSize, glyphSize, it.position*pixelSize)
        }
        verticalGuides.forEach {
            g.color = it.color
            g.drawLine(it.position*pixelSize, 0, it.position*pixelSize, glyphSize)
        }
    }

    @FXML
    fun canvasMouseDragged(e: MouseEvent) {
        val pos = pixelCoords(Pos(e.x.toInt(), e.y.toInt()))
        val mouseDragType = mouseDragType ?: return
        if(mouseDragType != glyph[pos.x, pos.y]) {
            glyph[pos.x, pos.y] = mouseDragType
            redrawCanvas()
        }
    }

    @FXML
    fun canvasMouseMoved(e: MouseEvent) {

    }

    @FXML
    fun canvasMousePressed(e: MouseEvent) {
        val pos = pixelCoords(Pos(e.x.toInt(), e.y.toInt()))
        mouseDragType = !glyph[pos.x, pos.y]
        glyph[pos.x, pos.y] = mouseDragType!!
        redrawCanvas()
    }

    @FXML
    fun canvasMouseReleased(e: MouseEvent) {
        mouseDragType = null
    }
}

data class Pos(val x: Int, val y: Int) {
    operator fun plus(other: Pos): Pos {
        return Pos(x + other.x, y + other.y)
    }

    operator fun minus(other: Pos): Pos {
        return Pos(x - other.x, y - other.y)
    }

    operator fun unaryMinus(): Pos {
        return Pos(-x, -y)
    }

    operator fun times(other: Pos): Pos {
        return Pos(x*other.x, y*other.y)
    }

    operator fun times(other: Number): Pos {
        return Pos((x*other.toDouble()).toInt(), (y*other.toDouble()).toInt())
    }

    operator fun div(other: Pos): Pos {
        return Pos(x/other.x, y/other.y)
    }

    operator fun div(other: Number): Pos {
        return Pos((x/other.toDouble()).toInt(), (y/other.toDouble()).toInt())
    }
}

var Graphics2D.strokeWidth: Float
    get() = (this.stroke as? BasicStroke)?.lineWidth ?: 1f
    set(value) {
        val current = this.stroke as? BasicStroke
        if(current != null) {
            this.stroke = BasicStroke(value, current.endCap, current.lineJoin,
                current.miterLimit, current.dashArray, current.dashPhase)
        } else {
            this.stroke = BasicStroke(value)
        }
    }
fun Graphics2D.loadIdentity() {
    this.transform = AffineTransform()
}
