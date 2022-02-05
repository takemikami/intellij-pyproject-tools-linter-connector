package com.github.takemikami.intellij.plugin.externallinters;

public class LinterProblem {
    public String filename;
    public int lineno;
    public int colno;
    public String errorCode;
    public String errorLevel;
    public String message;

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
