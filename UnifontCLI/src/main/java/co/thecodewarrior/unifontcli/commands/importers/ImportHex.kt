package co.thecodewarrior.unifontcli.commands.importers

import co.thecodewarrior.unifontlib.HexFile
import co.thecodewarrior.unifontlib.Unifont
import java.nio.file.Path

object ImportHex: Importer(
        name = "hex"
) {
    override fun run() {
        val inputHex = HexFile(unifont, file.toPath())
        inputHex.load()
    }
}