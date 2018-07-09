package co.thecodewarrior.unifontcli.commands

import com.github.ajalt.clikt.core.CliktCommand
import co.thecodewarrior.unifontlib.Unifont
import java.nio.file.Paths

abstract class UnifontCommand(
        help: String = "",
        epilog: String = "",
        name: String? = null,
        invokeWithoutSubcommand: Boolean = false
): CliktCommand(help, epilog, name, invokeWithoutSubcommand) {
    protected val unifont = Unifont(Paths.get("."))
    init {
        unifont.load()
    }
}