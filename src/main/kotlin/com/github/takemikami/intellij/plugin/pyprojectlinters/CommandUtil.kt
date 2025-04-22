package com.github.takemikami.intellij.plugin.pyprojectlinters

import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.StandardCharsets

/**
 * Command Utility for Inspection.
 */
class CommandUtil {
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
            cmd: Array<String?>,
            workingdir: String?,
            env: MutableMap<String?, String?>?,
            stdin: String?,
        ): String? {
            val pb = ProcessBuilder(*cmd)
            pb.directory(File(workingdir))
            if (env != null) {
                pb.environment().putAll(env)
            }
            val p = pb.start()
            if (stdin != null) {
                val out: OutputStream = p.getOutputStream()
                out.write(stdin.toByteArray(StandardCharsets.UTF_8))
                out.flush()
                out.close()
            }
            try {
                p.waitFor()
            } catch (ex: InterruptedException) {
                return null
            }
            val stderr = String(p.getErrorStream().readAllBytes(), StandardCharsets.UTF_8)
            return String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
        }
    }
}
