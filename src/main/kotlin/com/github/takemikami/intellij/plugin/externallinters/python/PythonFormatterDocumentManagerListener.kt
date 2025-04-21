package com.github.takemikami.intellij.plugin.externallinters.python

import com.github.takemikami.intellij.plugin.externallinters.CommandUtil
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

class PythonFormatterDocumentManagerListener(project: Project) : FileDocumentManagerListener {
    private val project: Project = project

    override fun beforeAllDocumentsSaving() {
        val unsavedDocuments: MutableList<Document?> =
            FileDocumentManager.getInstance().getUnsavedDocuments().toMutableList()

        // if (this.project == null) return
        val psiDocumentManager = PsiDocumentManager.getInstance(this.project)

        unsavedDocuments.forEach(
            Consumer { d: Document ->
                val file = psiDocumentManager.getPsiFile(d)
                if (file == null) return@Consumer
                val sdk = PythonInspectionUtil.getSdkFromFile(file)
                if (sdk == null) return@Consumer
                if (!file.language.isKindOf("Python")) return@Consumer

                val basePath: String = PythonInspectionUtil.getBasePathFromFile(file)!!
                val path: String? =
                    Objects.requireNonNull(file.getVirtualFile().getCanonicalPath())
                        ?.substring(basePath.length + 1)
                val body = file.getText()
                try {
                    val lines: MutableList<String> =
                        Files.readAllLines(
                            Path.of(basePath + "/pyproject.toml"),
                            StandardCharsets.UTF_8,
                        )

                    var newBody: String? = body

                    // black
                    val enableBlack =
                        lines.stream()
                            .anyMatch { ln: String? -> ln!!.trim { it <= ' ' }.startsWith("[tool.black]") }
                    val commandBinBlack = PythonInspectionUtil.getPythonCommandBin(sdk, "black")
                    if (enableBlack && commandBinBlack != null) {
                        newBody =
                            CommandUtil.runCommand(
                                arrayOf<String?>(
                                    commandBinBlack,
                                    "-",
                                ),
                                basePath,
                                null,
                                newBody,
                            )
                    }
                    // isort
                    val enableIsort =
                        lines.stream()
                            .anyMatch { ln: String? -> ln!!.trim { it <= ' ' }.startsWith("[tool.isort]") }
                    val commandBinIsort = PythonInspectionUtil.getPythonCommandBin(sdk, "isort")
                    if (enableIsort && commandBinIsort != null) {
                        newBody =
                            CommandUtil.runCommand(
                                arrayOf<String?>(
                                    commandBinIsort,
                                    "-",
                                ),
                                basePath,
                                null,
                                newBody,
                            )
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
                    println("Error: " + e.message)
                }
            },
        )
    }
}
