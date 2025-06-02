package com.github.takemikami.intellij.plugin.pyprojectlinters

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Local Inspection of ruff.
 */
class RuffLocalInspection : AbstractPythonInspection() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor {
        return buildPsiElementVisitorByCommand(holder, isOnTheFly, "ruff")
    }

    override fun isEnabledByPyprojectToml(tomlBody: String): Boolean {
        return tomlBody.split("\n").stream()
            .anyMatch { ln -> ln.trim().startsWith("[tool.ruff.lint]") || ln.trim().startsWith("[tool.ruff.lint.") }
    }

    val outputPattern: Pattern =
        Pattern.compile(
            "([^:]*):([^:]*):([^:]*):\\s*(.*)",
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
                "check",
                "--output-format",
                "concise",
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
        return LinterProblem(
            matcher.group(1),
            matcher.group(2).toInt(),
            matcher.group(3).toInt(),
            matcher.group(4),
        )
    }
}
