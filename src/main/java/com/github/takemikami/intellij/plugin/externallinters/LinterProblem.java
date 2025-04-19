package com.github.takemikami.intellij.plugin.externallinters;

/**
 * Problems class of external linter.
 */
public class LinterProblem {

  public String filename;
  public int lineno;
  public int colno;
  public int linenoEnd;
  public int colnoEnd;
  public String errorCode;
  public String errorLevel;
  public String message;

  /**
   * Problem object constructor.
   *
   * @param filename   target filename
   * @param lineno     line number of problem
   * @param colno      column index of problem
   * @param linenoEnd  line number of problem
   * @param colnoEnd   column index of problem
   * @param errorCode  problem code
   * @param errorLevel problem level
   * @param message    problem message
   */
  public LinterProblem(
      String filename,
      int lineno,
      int colno,
      int linenoEnd,
      int colnoEnd,
      String errorCode,
      String errorLevel,
      String message
  ) {
    this.filename = filename;
    this.lineno = lineno;
    this.colno = colno;
    this.linenoEnd = linenoEnd;
    this.colnoEnd = colnoEnd;
    this.errorCode = errorCode;
    this.errorLevel = errorLevel;
    this.message = message;
  }

  /**
   * Problem object constructor.
   *
   * @param filename   target filename
   * @param lineno     line number of problem
   * @param colno      column index of problem
   * @param linenoEnd  line number of problem
   * @param colnoEnd   column index of problem
   * @param errorCode  problem code
   * @param errorLevel problem level
   * @param message    problem message
   */
  public LinterProblem(
      String filename,
      int lineno,
      int colno,
      String errorCode,
      String errorLevel,
      String message
  ) {
    this.filename = filename;
    this.lineno = lineno;
    this.colno = colno;
    this.errorCode = errorCode;
    this.errorLevel = errorLevel;
    this.message = message;
  }
  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public int getLineno() {
    return lineno;
  }

  public void setLineno(int lineno) {
    this.lineno = lineno;
  }

  public int getColno() {
    return colno;
  }

  public void setColno(int colno) {
    this.colno = colno;
  }

  public int getLinenoEnd() {
    return linenoEnd;
  }

  public void setLinenoEnd(int linenoEnd) {
    this.linenoEnd = linenoEnd;
  }

  public int getColnoEnd() {
    return colnoEnd;
  }

  public void setColnoEnd(int colnoEnd) {
    this.colnoEnd = colnoEnd;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorLevel() {
    return errorLevel;
  }

  public void setErrorLevel(String errorLevel) {
    this.errorLevel = errorLevel;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
