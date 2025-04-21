package com.github.takemikami.intellij.plugin.externallinters.python

import com.github.takemikami.intellij.plugin.externallinters.CommandUtil
import com.github.takemikami.intellij.plugin.externallinters.LinterProblem
import com.github.takemikami.intellij.plugin.externallinters.TextOffsetDetector
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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
                val sdk = PythonInspectionUtil.getSdkFromFile(file)
                val commandBin = PythonInspectionUtil.getPythonCommandBin(sdk, commandName)
                if (commandBin == null) return
                if (!Files.exists(Paths.get(commandBin))) return
                val basePath: String = PythonInspectionUtil.getBasePathFromFile(file)!!
                try {
                    val lines: MutableList<String?> =
                        Files.readAllLines(
                            Path.of(basePath + "/pyproject.toml"),
                            StandardCharsets.UTF_8,
                        )
                    if (!isEnabledByPyprojectToml(java.lang.String.join("\n", lines))) {
                        return
                    }
                } catch (e: IOException) {
                    return
                }

                val path =
                    Objects.requireNonNull<String?>(file.getVirtualFile().getCanonicalPath())
                        .substring(basePath.length + 1)
                val body = file.getText()
                val detector = TextOffsetDetector(body)
                try {
                    val problems: MutableList<LinterProblem?> =
                        run(commandBin, basePath, path, body)!!
                    problems.forEach(
                        Consumer { problem: LinterProblem? ->
                            val offset = detector.getOffset(problem!!.lineno, problem.colno)
                            var offsetEnd = offset + 1
                            if (problem.linenoEnd != -1 && problem.colnoEnd != -1) {
                                offsetEnd =
                                    detector.getOffset(problem.linenoEnd, problem.colnoEnd) + 1
                            }
                            val range: TextRange? = TextRange(offset, offsetEnd)
                            val msg: String = commandName + ": " + problem.errorCode + " " + problem.message
                            holder.registerProblem(
                                file,
                                range,
                                msg,
                            )
                        },
                    )
                } catch (ex: IOException) {
                    // LOG.error("flake8 execution error, ", ex);
                }
            }
        }
    }

    @Throws(IOException::class)
    fun runLinter(
        binPath: String?,
        args: Array<String?>,
        workingDir: String?,
        env: MutableMap<String?, String?>?,
        path: String,
        body: String?,
        outputPattern: Pattern,
    ): MutableList<LinterProblem?> {
        val cmd: Array<String?>? = arrayOf<String?>(binPath).plus(args)
        val output = CommandUtil.runCommand(cmd!!, workingDir, env, body)
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
        }).filter(Objects::nonNull).toList()
    }

    open fun createTargetByMatcher(matcher: Matcher): String? {
        return null
    }

    abstract fun isEnabledByPyprojectToml(tomlBody: String?): Boolean

    @Throws(IOException::class)
    abstract fun run(
        binPath: String?,
        basePath: String?,
        path: String?,
        body: String?,
    ): MutableList<LinterProblem?>?

    abstract fun createLinterProblemByMatcher(matcher: Matcher): LinterProblem?
}
