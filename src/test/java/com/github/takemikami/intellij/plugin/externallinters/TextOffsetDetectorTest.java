package com.github.takemikami.intellij.plugin.externallinters;

import junit.framework.TestCase;

/**
 * Tests for TextOffsetDetector class.
 */
public class TextOffsetDetectorTest extends TestCase {

  public void testTextOffsetDetector() {
    String s = "aaaa\naaa\n\n\na";
    TextOffsetDetector tod = new TextOffsetDetector(s);
    assert tod.getOffset(1, 1) == 0;
    assert tod.getOffset(2, 1) == 5;
    assert tod.getOffset(10, 1) == s.length() - 1;

    assert tod.getOffset(2) == tod.getOffset(2, 1);
    assert tod.getOffset(3) == tod.getOffset(3, 1);
  }
}
