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

    override fun isEnabledByPyprojectToml(tomlBody: String?): Boolean {
        return tomlBody!!.split("\n").stream()
            .anyMatch { ln -> ln.trim().startsWith("[tool.ruff]") }
    }

    companion object {
        val OUTPUT_PATTERN: Pattern? =
            Pattern.compile(
                "([^:]*):([^:]*):([^:]*):\\s*(.*)",
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
                "check",
                "--output-format",
                "concise",
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
        return LinterProblem(
            m.group(1),
            m.group(2).toInt(),
            m.group(3).toInt(),
            m.group(4),
        )
    }
}
