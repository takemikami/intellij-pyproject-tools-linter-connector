package com.github.takemikami.intellij.plugin.externallinters

/**
 * Problems class of external linter.
 */
class LinterProblem(
    filename: String?,
    lineno: Int,
    colno: Int,
    linenoEnd: Int,
    colnoEnd: Int,
    errorCode: String?,
    errorLevel: String?,
    message: String?,
) {
    var filename: String? = filename
    var lineno: Int = lineno
    var colno: Int = colno
    var linenoEnd: Int = linenoEnd
    var colnoEnd: Int = colnoEnd
    var errorCode: String? = errorCode
    var errorLevel: String? = errorLevel
    var message: String? = message

    constructor(
        filename: String?,
        lineno: Int,
        colno: Int,
        errorCode: String?,
        errorLevel: String?,
        message: String?,
    ) : this(
        filename,
        lineno,
        colno,
        -1,
        -1,
        errorCode,
        errorLevel,
        message,
    )
}
