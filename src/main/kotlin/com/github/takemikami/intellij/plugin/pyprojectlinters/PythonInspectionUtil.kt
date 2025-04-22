package com.github.takemikami.intellij.plugin.pyprojectlinters

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiFile
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Utility for Python Inspection.
 */
class PythonInspectionUtil {
    companion object {
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
            val pythonBin = sdk.getHomeDirectory()
            if (pythonBin == null) {
                return null
            }
            val binDir = pythonBin.getParent().getCanonicalPath()
            val cmdbin = binDir + "/" + cmd
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
                return ProjectRootManager.getInstance(file.getProject()).getProjectSdk()
            }
            return ModuleRootManager.getInstance(mod).getSdk()
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
                return file.getProject().getBasePath()
            }
            return ModuleRootManager.getInstance(mod).getContentRoots()[0].getCanonicalPath()
        }
    }
}
