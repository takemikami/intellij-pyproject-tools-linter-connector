package com.github.takemikami.intellij.plugin.externallinters;

import org.apache.commons.lang.ArrayUtils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;

public class TextOffsetDetector {
    Integer[] linesLength;
    int maxLength;

    public TextOffsetDetector(String text) {
        maxLength = text.length();
        BufferedReader buff = new BufferedReader(new StringReader(text));
        linesLength = buff.lines().map(String::length).toArray(Integer[]::new);
    }

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
