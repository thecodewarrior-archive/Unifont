package co.thecodewarrior.unifontlib.utils

import kotlin.math.min

/**
 * Allows easily splitting a string into sections based on delimiters
 */
internal class Tokenizer(val string: String) {
    var index = 0
        private set(value) {
            field = min(value, string.length)
        }

    /**
     * Seeks the tokenizer to the specified position.
     *
     * @throws IllegalArgumentException if the index is larger than the length of [string]
     */
    fun seek(index: Int) {
        if(index > string.length)
            throw IllegalArgumentException("Index ($index) is larger than the length of the string (${string.length})")
        this.index = index
    }

    /**
     * Returns the remaining portion of the string and advances the tokenizer to the end of the string.
     * Returns an empty string if the tokenizer is already at the end of the string.
     */
    fun remaining(): String {
        if(index >= string.length) return ""
        val substr = string.substring(index, string.length)
        index = string.length
        return substr
    }

    //====================================================== Base ======================================================

    /**
     * Gets the portion of the string until the index returned by [find] (exclusive), or null if [find] returns -1.
     * Advances the tokenizer to the end of the returned string
     *
     * @return the substring or null
     */
    fun untilOrNull(find: (start: Int) -> Int): String? {
        if(index >= string.length) return null
        val i = find(index)
        if(i == -1) {
            return null
        } else {
            val substr = string.substring(index, i)
            index = i+1
            return substr
        }
    }

    /**
     * Gets the portion of the string until the index returned by [find] (exclusive)
     * Throws if the tokenizer is at the end of the string or [find] returns -1
     * Advances the tokenizer to the end of the returned string
     *
     * @throws IllegalArgumentException if the tokenizer is at the end of the string
     */
    fun until(find: (start: Int) -> Int) = untilOrNull(find) ?: throw IllegalArgumentException("Tokenizer already empty")

    /**
     * Gets the portion of the string until the index returned by [find] (exclusive), or the remainder of the string if
     * [find] returns -1.
     * Advances the tokenizer to the end of the returned string if [delimiter] was found
     *
     * @return the substring or the rest of the string
     */
    fun untilOrRemaining(find: (start: Int) -> Int): String {
        val match = untilOrNull(find)
        if(match == null) {
            val remaining = string.substring(index, string.length)
            index = string.length
            return remaining
        }
        return match
    }

    /**
     * Gets the match at the start of the string that matches [regex].
     * If [regex] does not begin with a start of string anchor (`^`), one will be added.
     *
     * @return the match or null if the tokenizer is at the end of the string or there wasn't a match
     */
    fun getMatch(regex: Regex): MatchResult? {
        if(index >= string.length) return null

        var anchoredRegex = regex
        if(!regex.pattern.startsWith("^")) {
            anchoredRegex = "^${regex.pattern}".toRegex(regex.options)
        }

        val match = anchoredRegex.find(this.string.substring(index))
        if(match != null) {
            index += match.value.length
        }
        return match
    }

    //====================================================== Char ======================================================

    /**
     * Gets the portion of the string until the next occurrence of [delimiter], or null if [delimiter] could not be found.
     * Advances the tokenizer to the end of the returned string.
     *
     * @return the substring or null
     */
    fun untilOrNull(delimiter: Char) = untilOrNull { string.indexOf(delimiter, startIndex = it) }

    /**
     * Gets the portion of the string until the next occurrence of [delimiter].
     * Throws if the tokenizer is at the end of the string or [delimiter] could not be found.
     * Advances the tokenizer to the end of the returned string.
     *
     * @throws IllegalArgumentException if the tokenizer is at the end of the string
     */
    fun until(delimiter: Char) = until { string.indexOf(delimiter, startIndex = it) }

    /**
     * Gets the portion of the string until the next occurrence of [delimiter] or the remainder of the string if
     * [delimiter] could not be found.
     * Advances the tokenizer to the end of the returned string.
     *
     * @return the substring or the rest of the string
     */
    fun untilOrRemaining(delimiter: Char) = until { string.indexOf(delimiter, startIndex = it) }

    //===================================================== String =====================================================

    /**
     * Gets the portion of the string until the next occurrence of [delimiter], or null if [delimiter] could not be found.
     * Advances the tokenizer to the end of the returned string.
     *
     * @return the substring or null
     */
    fun untilOrNull(delimiter: String) = untilOrNull { string.indexOf(delimiter, startIndex = it) }

    /**
     * Gets the portion of the string until the next occurrence of [delimiter].
     * Throws if the tokenizer is at the end of the string or [delimiter] could not be found.
     * Advances the tokenizer to the end of the returned string.
     *
     * @throws IllegalArgumentException if the tokenizer is at the end of the string
     */
    fun until(delimiter: String) = until { string.indexOf(delimiter, startIndex = it) }

    /**
     * Gets the portion of the string until the next occurrence of [delimiter] or the remainder of the string if
     * [delimiter] could not be found.
     * Advances the tokenizer to the end of the returned string.
     *
     * @return the substring or the rest of the string
     */
    fun untilOrRemaining(delimiter: String) = until { string.indexOf(delimiter, startIndex = it) }

    //====================================================== Regex ======================================================

    /**
     * Gets the portion of the string until the next occurrence of [delimiter], or null if [delimiter] could not be found.
     * Advances the tokenizer to the end of the returned string.
     *
     * @return the substring or null
     */
    fun untilOrNull(delimiter: Regex) = untilOrNull {
        delimiter.find(string, startIndex = it)?.range?.start ?: -1
    }

    /**
     * Gets the portion of the string until the next occurrence of [delimiter].
     * Throws if the tokenizer is at the end of the string or [delimiter] could not be found.
     * Advances the tokenizer to the end of the returned string.
     *
     * @throws IllegalArgumentException if the tokenizer is at the end of the string
     */
    fun until(delimiter: Regex) = until {
        delimiter.find(string, startIndex = it)?.range?.start ?: -1
    }

    /**
     * Gets the portion of the string until the next occurrence of [delimiter] or the remainder of the string if
     * [delimiter] could not be found.
     * Advances the tokenizer to the end of the returned string.
     *
     * @return the substring or the rest of the string
     */
    fun untilOrRemaining(delimiter: Regex) = until {
        delimiter.find(string, startIndex = it)?.range?.start ?: -1
    }
}
