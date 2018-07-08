package co.thecodewarrior.unifontcli.utils

fun String.removeEscapedNewlines(): String {
    return this.replace("\\\n", "")
}

fun String.splitOnce(vararg delimiters: Char, ignoreCase: Boolean = false): Pair<String, String?> {
    val split = this.split(*delimiters, ignoreCase = ignoreCase, limit = 1)
    return split[0] to split.getOrElse(0) { "" }
}