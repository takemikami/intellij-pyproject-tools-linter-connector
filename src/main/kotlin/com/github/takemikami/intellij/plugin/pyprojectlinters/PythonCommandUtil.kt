package com.github.takemikami.intellij.plugin.pyprojectlinters

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiFile
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Command Utility for Inspection.
 */
class PythonCommandUtil {
    companion object {
        /**
         * Execute process.
         *
         * @param cmd        command and parameters
         * @param workingdir working directory path
         * @param env        environment variables
         * @param stdin      standard in text
         * @return standard output string
         * @throws IOException execution failed
         */
        @Throws(IOException::class)
        fun runCommand(
            cmd: Array<String>,
            workingDir: String,
            env: MutableMap<String?, String?>?,
            stdin: String?,
            checkExit: Boolean = false,
        ): String? {
            val pb = ProcessBuilder(*cmd)
            pb.directory(File(workingDir))
            if (env != null) {
                pb.environment().putAll(env)
            }
            val p = pb.start()
            if (stdin != null) {
                val out: OutputStream = p.outputStream
                out.write(stdin.toByteArray(StandardCharsets.UTF_8))
                out.flush()
                out.close()
            }
            if (checkExit) {
                try {
                    val exitCode = p.waitFor()
                    if (exitCode != 0) {
                        return null
                    }
                } catch (ex: InterruptedException) {
                    return null
                }
            }
            val stderr = String(p.errorStream.readAllBytes(), StandardCharsets.UTF_8)
            return String(p.inputStream.readAllBytes(), StandardCharsets.UTF_8)
        }

        /**
         * Return command path in python bin path.
         *
         * @param sdk sdk object
         * @param cmd command name
         * @return command path
         */
        fun getPythonCommandBin(
            sdk: Sdk?,
            cmd: String?,
        ): String? {
            if (sdk == null) {
                return null
            }
            val pythonBin = sdk.homeDirectory
            if (pythonBin == null) {
                return null
            }
            val binDir = pythonBin.parent.canonicalPath
            val cmdbin = "$binDir/$cmd"
            if (!Files.exists(Paths.get(cmdbin))) {
                return null
            }
            return cmdbin
        }

        /**
         * Return sdk object according to module of file.
         *
         * @param file target file
         * @return sdk object
         */
        fun getSdkFromFile(file: PsiFile): Sdk? {
            val mod: Module? = ModuleUtil.findModuleForFile(file)
            if (mod == null) {
                return ProjectRootManager.getInstance(file.project).projectSdk
            }
            return ModuleRootManager.getInstance(mod).sdk
        }

        /**
         * Return base path according to module of file.
         *
         * @param file target file
         * @return module base path
         */
        fun getBasePathFromFile(file: PsiFile): String? {
            val mod: Module? = ModuleUtil.findModuleForFile(file)
            if (mod == null) {
                return file.project.basePath
            }
            return ModuleRootManager.getInstance(mod).contentRoots[0].canonicalPath
        }
    }
}
