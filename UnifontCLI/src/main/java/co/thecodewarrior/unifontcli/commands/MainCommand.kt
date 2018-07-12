package co.thecodewarrior.unifontcli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption

class MainCommand: CliktCommand() {
    init {
        this.versionOption(VERSION)
        this.subcommands(ReloadUCD(), Export(), Import())
    }

    override fun run() {
    }

    companion object {
        val VERSION = MainCommand::class.java.`package`.implementationVersion ?: "DEV"
    }
}