package co.thecodewarrior.unifontcli.commands

import co.thecodewarrior.unifontcli.commands.exporters.ExportGuides
import co.thecodewarrior.unifontcli.commands.exporters.ExportPic
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class Export: CliktCommand() {
    init {
        this.subcommands(ExportPic(), ExportGuides())
    }

    override fun run() {
    }
}
