package com.github.takemikami.intellij.plugin.pyprojectlinters

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Local Inspection of pylint.
 */
class PylintLocalInspection : AbstractPythonInspection() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor {
        return buildPsiElementVisitorByCommand(holder, isOnTheFly, "pylint")
    }

    override fun isEnabledByPyprojectToml(tomlBody: String?): Boolean {
        return tomlBody!!.split("\n").stream()
            .anyMatch { ln -> ln.trim().startsWith("[tool.pylint.") }
    }

    companion object {
        val OUTPUT_PATTERN: Pattern? =
            Pattern.compile(
                "([^:]*):([^:]*):([^:]*):([^:]*):([^:]*):\\s*([A-Z0-9]*):\\s*(.*)",
            )
    }

    override fun run(
        binPath: String?,
        basePath: String?,
        path: String?,
        body: String?,
    ): MutableList<LinterProblem?>? {
        return runLinter(
            binPath,
            arrayOf<String?>(
                "--msg-template='{path}:{line}:{column}:{end_line}:{end_column}: {msg_id}: {msg}'",
                "--from-stdin",
                path,
            ),
            basePath,
            null,
            path!!,
            body,
            OUTPUT_PATTERN!!,
        )
    }

    override fun createLinterProblemByMatcher(m: Matcher): LinterProblem? {
        val msg = m.group(6) + " " + m.group(7)
        try {
            return LinterProblem(
                m.group(1),
                m.group(2).toInt(),
                m.group(3).toInt() + 1,
                m.group(4).toInt(),
                m.group(5).toInt(),
                msg,
            )
        } catch (e: NumberFormatException) {
            try {
                return LinterProblem(
                    m.group(1),
                    m.group(2).toInt(),
                    m.group(3).toInt() + 1,
                    msg,
                )
            } catch (e2: NumberFormatException) {
                return null
            }
        }
    }
}
