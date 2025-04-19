package com.github.takemikami.intellij.plugin.externallinters;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Text offset detector, from line-number and column-number to offset.
 */
public class TextOffsetDetector {

  private final Integer[] linesLength;
  private final int maxLength;

  /**
   * TextOffsetDetector constructor.
   *
   * @param text text body
   */
  public TextOffsetDetector(String text) {
    maxLength = text.length();
    BufferedReader buff = new BufferedReader(new StringReader(text));
    linesLength = buff.lines().map(String::length).toArray(Integer[]::new);
  }

  /**
   * Return offset in text body.
   *
   * @param line line number
   * @param col  column number
   * @return offset
   */
  public int getOffset(int line, int col) {
    int offset = Arrays.stream(
        ArrayUtils.subarray(linesLength, 0, line - 1)
    ).map(l -> (int) l + 1).reduce(Integer::sum).orElse(0) + col - 1;
    return Math.min(offset, maxLength - 1);
  }

  public int getOffset(int line) {
    return getOffset(line, 1);
  }

}
