package co.thecodewarrior.unifontcli.commands.importers

import co.thecodewarrior.unifontcli.commands.UnifontCommand
import co.thecodewarrior.unifontlib.Unifont
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import java.nio.file.Path

abstract class Importer(name: String? = null, epilog: String = "", help: String = ""):
        UnifontCommand(name = name, epilog = epilog, help = help) {

    val file by argument(name = "file").file(exists = true, readable = true)
}