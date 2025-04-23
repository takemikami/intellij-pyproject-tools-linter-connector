package com.github.takemikami.intellij.plugin.pyprojectlinters

/**
 * Problems class of external linter.
 */
class LinterProblem(
    filename: String?,
    lineno: Int,
    colno: Int,
    linenoEnd: Int,
    colnoEnd: Int,
    message: String?,
) {
    var filename: String? = filename
    var lineno: Int = lineno
    var colno: Int = colno
    var linenoEnd: Int = linenoEnd
    var colnoEnd: Int = colnoEnd
    var message: String? = message

    constructor(
        filename: String?,
        lineno: Int,
        colno: Int,
        message: String?,
    ) : this(
        filename,
        lineno,
        colno,
        -1,
        -1,
        message,
    )
}
