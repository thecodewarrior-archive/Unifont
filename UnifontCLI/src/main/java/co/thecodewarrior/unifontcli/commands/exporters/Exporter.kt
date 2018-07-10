package co.thecodewarrior.unifontcli.commands.exporters

import co.thecodewarrior.unifontcli.commands.UnifontCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file

abstract class Exporter(name: String? = null, epilog: String = "", help: String = ""):
        UnifontCommand(name = name, epilog = epilog, help = help) {

    val file by argument(name = "file").file()
}
