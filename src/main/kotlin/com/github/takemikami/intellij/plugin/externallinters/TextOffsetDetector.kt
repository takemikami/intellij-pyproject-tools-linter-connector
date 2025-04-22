package com.github.takemikami.intellij.plugin.externallinters

import java.io.BufferedReader
import java.io.StringReader
import kotlin.math.min

/**
 * Text offset detector, from line-number and column-number to offset.
 */
class TextOffsetDetector(text: String) {
    private val maxLength = text.length
    val buff = BufferedReader(StringReader(text))
    private val linesLength = buff.lines().map { obj: String? -> obj!!.length }.toList() as List<Int>

    /**
     * Return offset in text body.
     *
     * @param line line number
     * @param col  column number
     * @return offset
     */
    fun getOffset(
        line: Int,
        col: Int,
    ): Int {
        val offsetLine =
            linesLength
                .take(line - 1)
                .map { l -> l + 1 }
                .reduceOrNull { a, b -> a + b }
                ?: 0
        val offset = offsetLine + col - 1
        return min(offset, maxLength - 1)
    }

    fun getOffset(line: Int): Int {
        return getOffset(line, 1)
    }
}
