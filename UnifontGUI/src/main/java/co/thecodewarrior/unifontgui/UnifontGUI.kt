package co.thecodewarrior.unifontgui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class UnifontGUI: Application() {

    override fun start(primaryStage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("Main.fxml"))
        val root = loader.load<Parent>()
        val scene = Scene(root)
        primaryStage.scene = scene
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(UnifontGUI::class.java, *args)
        }
    }
}
