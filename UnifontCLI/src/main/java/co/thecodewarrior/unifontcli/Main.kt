package co.thecodewarrior.unifontcli

import co.thecodewarrior.unifontcli.commands.MainCommand

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true");
    MainCommand().main(args)
}
