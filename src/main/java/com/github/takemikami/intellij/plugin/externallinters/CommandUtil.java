package com.github.takemikami.intellij.plugin.externallinters;

import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CommandUtil {
    private static final Logger LOG = Logger.getInstance(CommandUtil.class);

    public static String runCommand(String[] cmd, String workingdir, Map<String, String> env) throws IOException {
        return CommandUtil.runCommand(cmd, workingdir, env, null);
    }

    public static String runCommand(String[] cmd, String workingdir, Map<String, String> env, String stdin) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(workingdir));
        if (env != null) {
            pb.environment().putAll(env);
        }
        Process p = pb.start();
        if (stdin != null) {
            OutputStream out = p.getOutputStream();
            out.write(stdin.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        }
        try {
            p.waitFor();
        } catch (InterruptedException ex) {
            LOG.error("command " + cmd[0] + " error: ", ex);
            return "";
        }
        String stderr = new String(p.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        LOG.debug("command " + cmd[0] + " stderr: ", stderr);
        return new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }


}

