package com.github.takemikami.intellij.plugin.externallinters.python;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Files;
import java.nio.file.Paths;

public class PythonInspectionUtil {
    private static final Logger LOG = Logger.getInstance(PythonInspectionUtil.class);

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
}
