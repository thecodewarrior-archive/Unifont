package co.thecodewarrior.unifontcli.commands

import co.thecodewarrior.unifontcli.commands.importers.ImportGuides
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class Import: CliktCommand() {
    init {
        this.subcommands(ImportGuides())
    }

    override fun run() {
    }
}
