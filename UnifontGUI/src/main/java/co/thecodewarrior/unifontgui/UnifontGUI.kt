package co.thecodewarrior.unifontgui

import co.thecodewarrior.unifontgui.controllers.Main
import co.thecodewarrior.unifontlib.Unifont
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import java.nio.file.Paths

class UnifontGUI: Application() {

    override fun start(primaryStage: Stage) {
        Main.open(primaryStage)
        primaryStage.close()
//        val loader = FXMLLoader(javaClass.getResource("Main.fxml"))
//        val root = loader.load<Parent>()
//        val scene = Scene(root)
//        primaryStage.scene = scene
//        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(UnifontGUI::class.java, *args)
        }
    }
}
