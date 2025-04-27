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

    override fun isEnabledByPyprojectToml(tomlBody: String): Boolean {
        return tomlBody.split("\n").stream()
            .anyMatch { ln -> ln.trim().startsWith("[tool.pylint.") }
    }

    val outputPattern: Pattern =
        Pattern.compile(
            "([^:]*):([^:]*):([^:]*):([^:]*):([^:]*):\\s*([A-Z0-9]*):\\s*(.*)",
        )

    override fun run(
        binPath: String,
        basePath: String,
        path: String,
        body: String,
    ): List<LinterProblem> {
        return runLinter(
            binPath,
            arrayOf<String>(
                "--msg-template='{path}:{line}:{column}:{end_line}:{end_column}: {msg_id}: {msg}'",
                "--from-stdin",
                path,
            ),
            basePath,
            null,
            path,
            body,
            outputPattern,
        )
    }

    override fun createLinterProblemByMatcher(matcher: Matcher): LinterProblem? {
        val msg = matcher.group(6) + " " + matcher.group(7)
        try {
            return LinterProblem(
                matcher.group(1),
                matcher.group(2).toInt(),
                matcher.group(3).toInt() + 1,
                matcher.group(4).toInt(),
                matcher.group(5).toInt(),
                msg,
            )
        } catch (e: NumberFormatException) {
            try {
                return LinterProblem(
                    matcher.group(1),
                    matcher.group(2).toInt(),
                    matcher.group(3).toInt() + 1,
                    msg,
                )
            } catch (e2: NumberFormatException) {
                return null
            }
        }
    }
}
