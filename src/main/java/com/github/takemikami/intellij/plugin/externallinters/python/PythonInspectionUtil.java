package com.github.takemikami.intellij.plugin.externallinters.python;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility for Python Inspection.
 */
public class PythonInspectionUtil {

  private static final Logger LOG = Logger.getInstance(PythonInspectionUtil.class);

  /**
   * Return command path in python bin path.
   *
   * @param sdk sdk object
   * @param cmd command name
   * @return command path
   */
  public static String getPythonCommandBin(Sdk sdk, String cmd) {
    if (sdk == null) {
      return null;
    }
    VirtualFile pythonBin = sdk.getHomeDirectory();
    if (pythonBin == null) {
      return null;
    }
    String binDir = pythonBin.getParent().getCanonicalPath();
    String cmdbin = binDir + "/" + cmd;
    if (!Files.exists(Paths.get(cmdbin))) {
      return null;
    }
    LOG.debug("command detected. path=" + cmdbin);
    return cmdbin;
  }

  /**
   * Return sdk object according to module of file.
   *
   * @param file target file
   * @return sdk object
   */
  public static Sdk getSdkFromFile(PsiFile file) {
    Module mod = ModuleUtil.findModuleForFile(file);
    if (mod == null) {
      return ProjectRootManager.getInstance(file.getProject()).getProjectSdk();
    }
    return ModuleRootManager.getInstance(mod).getSdk();
  }

  /**
   * Return base path according to module of file.
   *
   * @param file target file
   * @return module base path
   */
  public static String getBasePathFromFile(PsiFile file) {
    Module mod = ModuleUtil.findModuleForFile(file);
    if (mod == null) {
      return file.getProject().getBasePath();
    }
    return ModuleRootManager.getInstance(mod).getContentRoots()[0].getCanonicalPath();
  }

}
