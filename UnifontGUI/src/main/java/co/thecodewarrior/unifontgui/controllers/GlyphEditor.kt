package co.thecodewarrior.unifontgui.controllers

import co.thecodewarrior.unifontgui.ChangeListener
import co.thecodewarrior.unifontgui.Changes
import co.thecodewarrior.unifontgui.Constants
import co.thecodewarrior.unifontgui.sizeHeightTo
import co.thecodewarrior.unifontlib.EditorGuide
import co.thecodewarrior.unifontlib.Glyph
import co.thecodewarrior.unifontlib.Unifont
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.CheckBox
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
import javafx.stage.Stage
import java.awt.BasicStroke
import java.awt.Font
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination



class GlyphEditor: ChangeListener {
    lateinit var stage: Stage
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
    var lastPos: Pos? = null
    var lastDragType: Boolean = false

    var pixelSize: Int = 16
    var glyphSize: Int = 0
    lateinit var glyphPos: Pos
    lateinit var referencePos: Pos
    var referenceFont: Font = Constants.notoSans

    lateinit var zoomMetric: Slider
    lateinit var advanceMetric: Slider
    lateinit var leftHangMetric: Slider
    lateinit var missingMetric: CheckBox

    fun setup(stage: Stage, project: Unifont, glyph: Glyph) {
        this.stage = stage
        this.project = project
        this.pixelSize = 256/project.settings.size
        this.horizontalGuides.addAll(project.settings.horizontalGuides)
        this.verticalGuides.addAll(project.settings.verticalGuides)

        stage.isResizable = false

        zoomMetric = createSlider("Zoom", 3, 32, pixelSize) {
            zoom(it, keepCanvasSize = true)
            redrawCanvas()
        }.also { slider ->
            slider.valueChangingProperty().addListener { _, oldValue, newValue ->
                if(oldValue && !newValue) {
                    zoom(pixelSize)
                    redrawCanvas()
                }
            }
        }

        missingMetric = createCheckbox("Missing", glyph.missing) {
            this.glyph.missing = it
            Changes.submit(this.glyph)
        }

        advanceMetric = createSlider("Advance", 0, project.settings.size + 1, glyph.advance) {
            this.glyph.advance = it
            Changes.submit(this.glyph)
        }
        leftHangMetric = createSlider("Left overhang", 0, project.settings.size, glyph.leftHang) {
            this.glyph.leftHang = it
            Changes.submit(this.glyph)
        }

        zoom(pixelSize)
        loadGlyph(glyph)

        redrawCanvas()
    }

    @FXML
    fun nextGlyph() {
        val file = project.fileForCodepoint(glyph.codepoint+1)
        if(!file.loaded) file.load()
        val newGlyph = file.glyphs[glyph.codepoint+1] ?: return
        loadGlyph(newGlyph)
        redrawCanvas()
    }

    @FXML
    fun previousGlyph() {
        val file = project.fileForCodepoint(glyph.codepoint-1)
        if(!file.loaded) file.load()
        val newGlyph = file.glyphs[glyph.codepoint-1] ?: return
        loadGlyph(newGlyph)
        redrawCanvas()
    }

    fun loadGlyph(glyph: Glyph) {
        try {
            this.unlistenTo(this.glyph)
        } catch(e: UninitializedPropertyAccessException) {
            // nop
        }
        this.glyph = glyph
        this.listenTo(glyph)

        advanceMetric.value = glyph.advance.toDouble()
        leftHangMetric.value = glyph.leftHang.toDouble()
        missingMetric.isSelected = glyph.missing
        zoom(pixelSize)
    }

    fun createSlider(name: String, min: Int, max: Int, initialValue: Int, change: (Int) -> Unit): Slider {
        val label = Label(name)
        label.textAlignment = TextAlignment.CENTER
        label.prefWidth = 200.0
        val slider = Slider(min.toDouble(), max.toDouble(), initialValue.toDouble())
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
        return slider
    }

    fun createCheckbox(name: String, initialValue: Boolean, change: (Boolean) -> Unit): CheckBox {
        val checkbox = CheckBox(name)
        checkbox.isSelected = initialValue
        var lastValue = initialValue
        checkbox.selectedProperty().addListener { _, _, value ->
            if(value != lastValue) {
                lastValue = value
                change(value)
            }
        }

        metrics.children.add(checkbox)
        return checkbox
    }

    fun zoom(size: Int, keepCanvasSize: Boolean = false) {
        pixelSize = size
        glyphSize = project.settings.size*pixelSize
        glyphPos = Pos(2*glyphSize, glyphSize/2)
        referencePos = Pos(glyphSize/2, glyphSize/2)

        this.referenceFont = referenceFont.sizeHeightTo("X", pixelSize*project.settings.capHeight.toFloat())
        if(!keepCanvasSize) {
            canvas.width = glyphSize * 3.5
            canvas.height = glyphSize * 2.0
        }

        stage.sizeToScene()
    }

    fun pixelCoords(canvasPos: Pos): Pos {
        return (canvasPos - glyphPos) / pixelSize
    }

    fun shutdown() {
        unlistenTo(this.glyph)
    }

    override fun changeOccured(target: Any) {
        glyph.missing = false
        advanceMetric.value = glyph.advance.toDouble()
        leftHangMetric.value = glyph.leftHang.toDouble()
        missingMetric.isSelected = glyph.missing
        glyph.markDirty()
        redrawCanvas()
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
        this.lastPos = pos
        val mouseDragType = mouseDragType ?: return
        if(mouseDragType != glyph[pos.x, pos.y]) {
            glyph[pos.x, pos.y] = mouseDragType
            Changes.submit(glyph)
        }
    }

    @FXML
    fun canvasMouseMoved(e: MouseEvent) {

    }

    @FXML
    fun canvasMousePressed(e: MouseEvent) {
        val pos = pixelCoords(Pos(e.x.toInt(), e.y.toInt()))
        val lastPos = lastPos
        if(lastPos != null && e.isShiftDown) {
            lastPos.lineTo(pos).forEach { pixel ->
                glyph[pixel.x, pixel.y] = lastDragType
            }
            mouseDragType = lastDragType
        } else {
            mouseDragType = !glyph[pos.x, pos.y]
            glyph[pos.x, pos.y] = mouseDragType!!
        }
        this.lastDragType = mouseDragType!!
        this.lastPos = pos
        Changes.submit(glyph)
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

    fun lineTo(other: Pos): List<Pos> {
        val x1 = x
        val y1 = y
        val x2 = other.x
        val y2 = other.y

        var d = 0
        val dy = Math.abs(y2 - y1)
        val dx = Math.abs(x2 - x1)
        val dy2 = dy shl 1
        val dx2 = dx shl 1
        val ix = if (x1 < x2)  1 else -1
        val iy = if (y1 < y2)  1 else -1
        var xx = x1
        var yy = y1

        val list = mutableListOf<Pos>()
        if (dy <= dx) {
            while (true) {
                list.add(Pos(xx, yy))
                if (xx == x2) break
                xx += ix
                d  += dy2
                if (d > dx) {
                    yy += iy
                    d  -= dx2
                }
            }
        }
        else {
            while (true) {
                list.add(Pos(xx, yy))
                if (yy == y2) break
                yy += iy
                d  += dx2
                if (d > dy) {
                    xx += ix
                    d  -= dy2
                }
            }
        }
        return list
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
