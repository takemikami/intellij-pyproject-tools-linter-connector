package com.github.takemikami.intellij.plugin.pyprojectlinters

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Local Inspection of flake8.
 */
class Flake8LocalInspection : AbstractPythonInspection() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor {
        return buildPsiElementVisitorByCommand(holder, isOnTheFly, "pflake8")
    }

    override fun isEnabledByPyprojectToml(tomlBody: String): Boolean {
        return tomlBody.split("\n").stream()
            .anyMatch { ln -> ln.trim().startsWith("[tool.flake8]") }
    }

    val outputPattern: Pattern =
        Pattern.compile(
            "([^:]*):([^:]*):([^:]*):\\s*([A-Z0-9]*)\\s*(.*)",
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
                "--format",
                "'%(path)s:%(row)d:%(col)d: %(code)s %(text)s'",
                "-",
            ),
            basePath,
            null,
            path,
            body,
            outputPattern,
        )
    }

    override fun createLinterProblemByMatcher(matcher: Matcher): LinterProblem? {
        val msg = matcher.group(4) + " " + matcher.group(5)
        return LinterProblem(
            matcher.group(1),
            matcher.group(2).toInt(),
            matcher.group(3).toInt(),
            msg,
        )
    }
}
