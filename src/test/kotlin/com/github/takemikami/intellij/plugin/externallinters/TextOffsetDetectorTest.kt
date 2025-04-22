package com.github.takemikami.intellij.plugin.externallinters

import junit.framework.TestCase

/**
 * Tests for TextOffsetDetector class.
 */
class TextOffsetDetectorTest : TestCase() {
    fun test() {
        val s = "aaaa\naaa\n\n\na"
        val tod = TextOffsetDetector(s)
        assert(tod.getOffset(1, 1) == 0)
        assert(tod.getOffset(2, 1) == 5)
        assert(tod.getOffset(2, 3) == 7)
        assert(tod.getOffset(10, 1) == s.length - 1)

        assert(tod.getOffset(2) == tod.getOffset(2, 1))
        assert(tod.getOffset(3) == tod.getOffset(3, 1))
    }
}
