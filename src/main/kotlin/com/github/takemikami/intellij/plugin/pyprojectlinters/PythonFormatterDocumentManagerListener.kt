package com.github.takemikami.intellij.plugin.pyprojectlinters

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.DocumentRunnable
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Objects
import java.util.function.Consumer

class PythonFormatterDocumentManagerListener(private val project: Project) : FileDocumentManagerListener {
    override fun beforeAllDocumentsSaving() {
        val unsavedDocuments: MutableList<Document?> =
            FileDocumentManager.getInstance().unsavedDocuments.toMutableList()

        // if (this.project == null) return
        val psiDocumentManager = PsiDocumentManager.getInstance(this.project)

        unsavedDocuments.forEach(
            Consumer { d: Document ->
                val file = psiDocumentManager.getPsiFile(d)
                if (file == null) return@Consumer
                val sdk = PythonCommandUtil.Companion.getSdkFromFile(file)
                if (sdk == null) return@Consumer
                if (!file.language.isKindOf("Python")) return@Consumer

                val basePath: String = PythonCommandUtil.Companion.getBasePathFromFile(file)!!
                val path: String? =
                    Objects.requireNonNull(file.virtualFile.canonicalPath)
                        ?.substring(basePath.length + 1)
                val body = file.getText()
                try {
                    val lines: MutableList<String> =
                        Files.readAllLines(
                            Path.of("$basePath/pyproject.toml"),
                            StandardCharsets.UTF_8,
                        )

                    var newBody: String? = body

                    data class Formatter(val commandName: String, val pyprojectPrefix: String, val args: Array<String>)
                    for (formatter in arrayOf<Formatter>(
                        Formatter("black", "[tool.black]", arrayOf<String>("-")),
                        Formatter("isort", "[tool.isort]", arrayOf<String>("-")),
                        Formatter("ruff", "[tool.ruff.format]", arrayOf<String>("format", "-")),
                        Formatter("ruff", "[tool.ruff.lint.isort]", arrayOf<String>("check", "--select", "I", "--fix", "-")),
                    )) {
                        val enableFormatter =
                            lines.stream()
                                .anyMatch { ln: String? ->
                                    ln!!.trim { it <= ' ' }.startsWith(formatter.pyprojectPrefix)
                                }
                        val commandBinFormatter =
                            PythonCommandUtil.Companion.getPythonCommandBin(sdk, formatter.commandName)
                        if (enableFormatter && commandBinFormatter != null) {
                            val cmd: Array<String> = arrayOf<String>(commandBinFormatter).plus(formatter.args)
                            val formatted =
                                PythonCommandUtil.runCommand(
                                    cmd,
                                    basePath,
                                    null,
                                    newBody,
                                    true,
                                )
                            if (formatted != null) {
                                newBody = formatted
                            }
                        }
                    }

                    if (newBody != null && body != newBody) {
                        val output = newBody
                        val bodyLength = body.length

                        val application: Application = ApplicationManager.getApplication()
                        application.runWriteAction(
                            object : DocumentRunnable(d, null) {
                                override fun run() {
                                    CommandProcessor.getInstance()
                                        .runUndoTransparentAction(
                                            object : Runnable {
                                                override fun run() {
                                                    d.replaceString(0, bodyLength, output)
                                                }
                                            },
                                        )
                                }
                            },
                        )
                    }
                } catch (e: IOException) {
                    // nothing to do
                }
            },
        )
    }
}
