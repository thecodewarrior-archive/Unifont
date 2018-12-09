package co.thecodewarrior.unifontcli.commands.exporters.ttf

import co.thecodewarrior.unifontcli.commands.exporters.Exporter
import co.thecodewarrior.unifontcli.utils.loadWithProgress
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import me.tongfei.progressbar.ProgressBar
import java.awt.Dimension
import java.awt.Font
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JTextField

class ExportTTF: Exporter(name="font") {
    val test by option("-t", "--test").flag("--no-test")

    override fun run() {
        unifont.all.loadWithProgress()
        file.deleteRecursively()
        val generator = UnifontTTFGenerator(file.name, file.absoluteFile)

        ProgressBar("Trace glyphs", unifont.all.sumBy { it.glyphs.count { !it.value.missing }}.toLong()).use { progress ->
            unifont.all.forEach {
                progress.extraMessage = it.blockName
                it.glyphs.forEach glyph@{
                    if(it.value.missing) return@glyph
                    generator.addGlyph(it.value)
                    progress.stepBy(1L)
                }
            }
        }

        val font = generator.build()

        if(test && font != null) {
            val fontTester = FontTestFrame(
                font,
                "The quick brown fox jumped over the lazy dog."
            )

            fontTester.isVisible = true
        }
    }

}

class FontTestFrame(testFont: Font, text: String): JFrame("Font test") {
    init {
        setSize(300, 200)
        setLocationRelativeTo(null)
        defaultCloseOperation = EXIT_ON_CLOSE

        val label = JLabel(text)
        val textField = JTextField(text, 20)
        textField.addActionListener {
            label.text = "<html>${textField.text}</html>"
        }

        textField.maximumSize = Dimension(Int.MAX_VALUE, textField.preferredSize.height)

        val sizeField = JTextField("16", 20)
        sizeField.addActionListener {
            label.font = label.font.deriveFont(sizeField.text?.toFloatOrNull() ?: 16f)
        }

        sizeField.maximumSize = Dimension(Int.MAX_VALUE, sizeField.preferredSize.height)
        add(sizeField)
        add(textField)
        add(label)

        label.font = testFont.deriveFont(16f)

        contentPane.layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
    }
}
