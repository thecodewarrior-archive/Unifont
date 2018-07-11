package co.thecodewarrior.unifontcli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class MainCommand: CliktCommand() {
    init {
        this.subcommands(ReloadUCD(), Export(), Import())
    }

    override fun run() {
    }
}