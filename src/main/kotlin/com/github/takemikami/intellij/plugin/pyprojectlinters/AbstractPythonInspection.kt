package com.github.takemikami.intellij.plugin.pyprojectlinters

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Arrays
import java.util.Objects
import java.util.function.Consumer
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class AbstractPythonInspection : LocalInspectionTool() {
    fun buildPsiElementVisitorByCommand(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        commandName: String,
    ): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                super.visitFile(file)
                val sdk = PythonCommandUtil.Companion.getSdkFromFile(file)
                val commandBin = PythonCommandUtil.Companion.getPythonCommandBin(sdk, commandName)
                if (commandBin == null) return
                val basePath: String = PythonCommandUtil.Companion.getBasePathFromFile(file)!!
                try {
                    val tomlBody =
                        Files.readAllLines(
                            Path.of("$basePath/pyproject.toml"),
                            StandardCharsets.UTF_8,
                        ).joinToString("\n")
                    if (!isEnabledByPyprojectToml(tomlBody)) {
                        return
                    }
                } catch (e: IOException) {
                    return
                }

                val path =
                    Objects.requireNonNull<String>(file.virtualFile.canonicalPath)
                        .substring(basePath.length + 1)
                val body = file.text
                val detector = TextOffsetDetector(body)
                try {
                    val problems: List<LinterProblem> =
                        run(commandBin, basePath, path, body)
                    problems.forEach(
                        Consumer { problem: LinterProblem ->
                            val offset = detector.getOffset(problem.lineno, problem.colno)
                            val offsetEnd =
                                if (problem.linenoEnd != -1 && problem.colnoEnd != -1) {
                                    detector.getOffset(problem.linenoEnd, problem.colnoEnd) + 1
                                } else {
                                    offset + 1
                                }
                            holder.registerProblem(
                                file,
                                TextRange(offset, offsetEnd),
                                "[" + commandName + "] " + problem.message,
                            )
                        },
                    )
                } catch (ex: IOException) {
                    // nothing to do
                }
            }
        }
    }

    @Throws(IOException::class)
    fun runLinter(
        binPath: String,
        args: Array<String>,
        workingDir: String,
        env: MutableMap<String?, String?>?,
        path: String,
        body: String?,
        outputPattern: Pattern,
    ): List<LinterProblem> {
        val cmd: Array<String> = arrayOf<String>(binPath).plus(args)
        val output = PythonCommandUtil.runCommand(cmd, workingDir, env, body)
        if (output == null) {
            return arrayListOf()
        }
        return Arrays.stream(
            output.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray(),
        ).map({ ln ->
            val m: Matcher = outputPattern.matcher(ln)
            if (!m.matches()) return@map null
            val problemFile = createTargetByMatcher(m)
            if (problemFile != null && path != problemFile) return@map null
            createLinterProblemByMatcher(m)
        }).filter(Objects::nonNull).toList().filterNotNull()
    }

    open fun createTargetByMatcher(matcher: Matcher): String? {
        return null
    }

    abstract fun isEnabledByPyprojectToml(tomlBody: String): Boolean

    @Throws(IOException::class)
    abstract fun run(
        binPath: String,
        basePath: String,
        path: String,
        body: String,
    ): List<LinterProblem>

    abstract fun createLinterProblemByMatcher(matcher: Matcher): LinterProblem?
}
