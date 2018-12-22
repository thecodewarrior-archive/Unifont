package co.thecodewarrior.unifontgui.utils

import co.thecodewarrior.unifontgui.Constants
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import java.awt.BasicStroke
import java.awt.Graphics2D
import java.awt.geom.AffineTransform

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

fun <T> openFXML(name: String, title: String, configure: (T, Stage) -> Unit) {
    val fxmlLoader = FXMLLoader(Constants.resource("$name.fxml"))
    val root = fxmlLoader.load<Parent>()
    val controller = fxmlLoader.getController<T>()
    val stage = Stage()
    stage.title = title
    stage.scene = Scene(root)
    configure(controller, stage)
    stage.show()
}

