package com.github.takemikami.intellij.plugin.pyprojectlinters

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
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

    override fun isEnabledByPyprojectToml(tomlBody: String?): Boolean {
        return tomlBody!!.split("\n").stream()
            .anyMatch { ln -> ln.trim().startsWith("[tool.mypy]") }
    }

    companion object {
        val OUTPUT_PATTERN: Pattern? =
            Pattern.compile(
                "([^:]*):([^:]*):([^:]*):([^:]*):([^:]*):\\s*([^:]*):\\s*(.*)",
            )
    }

    override fun run(
        binPath: String?,
        basePath: String?,
        path: String?,
        body: String?,
    ): MutableList<LinterProblem?>? {
        var tmp: Path? = null
        try {
            tmp = Files.createTempFile(path!!.substring(0, path.length - 3), ".py")
            val pw: PrintWriter = PrintWriter(FileWriter(tmp.toFile()))
            pw.write(body)
            pw.flush()
            pw.close()

            return runLinter(
                binPath,
                arrayOf<String?>(
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
                OUTPUT_PATTERN!!,
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

    override fun createLinterProblemByMatcher(m: Matcher): LinterProblem? {
        return LinterProblem(
            m.group(1),
            m.group(2).toInt(),
            m.group(3).toInt(),
            m.group(4).toInt(),
            m.group(5).toInt(),
            null,
            m.group(6),
            m.group(7),
        )
    }

    override fun createTargetByMatcher(m: Matcher): String? {
        return m.group(1)
    }
}
