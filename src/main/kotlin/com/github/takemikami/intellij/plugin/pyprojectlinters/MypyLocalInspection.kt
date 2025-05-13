package com.github.takemikami.intellij.plugin.pyprojectlinters

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Local Inspection of mypy.
 */
class MypyLocalInspection : AbstractPythonInspection() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor {
        return buildPsiElementVisitorByCommand(holder, isOnTheFly, "mypy")
    }

    override fun isEnabledByPyprojectToml(tomlBody: String): Boolean {
        return tomlBody.split("\n").stream()
            .anyMatch { ln -> ln.trim().startsWith("[tool.mypy]") }
    }

    val outputPattern: Pattern =
        Pattern.compile(
            "([^:]*):([^:]*):([^:]*):([^:]*):([^:]*):\\s*([^:]*):\\s*(.*)",
        )

    override fun run(
        binPath: String,
        basePath: String,
        path: String,
        body: String,
    ): List<LinterProblem> {
        var tmp: Path? = null
        try {
            tmp = Files.createTempFile(File(path).nameWithoutExtension, ".py")
            val pw: PrintWriter = PrintWriter(FileWriter(tmp.toFile()))
            pw.write(body)
            pw.flush()
            pw.close()

            return runLinter(
                binPath,
                arrayOf<String>(
                    "--show-column-numbers",
                    "--show-error-end",
                    "--no-color-output",
                    "--shadow-file",
                    path,
                    tmp.toFile().getAbsolutePath(),
                    path,
                ),
                basePath,
                null,
                path,
                null,
                outputPattern,
            )
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp.toAbsolutePath())
                } catch (ex: IOException) {
                    // nothing to do
                }
            }
        }
    }

    override fun createLinterProblemByMatcher(matcher: Matcher): LinterProblem? {
        val msg = matcher.group(6) + " " + matcher.group(7)
        return LinterProblem(
            matcher.group(1),
            matcher.group(2).toInt(),
            matcher.group(3).toInt(),
            matcher.group(4).toInt(),
            matcher.group(5).toInt(),
            msg,
        )
    }

    override fun createTargetByMatcher(matcher: Matcher): String? {
        return matcher.group(1)
    }
}
