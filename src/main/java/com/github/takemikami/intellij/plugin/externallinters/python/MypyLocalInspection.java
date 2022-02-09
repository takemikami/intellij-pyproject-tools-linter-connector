package com.github.takemikami.intellij.plugin.externallinters.python;

import com.github.takemikami.intellij.plugin.externallinters.CommandUtil;
import com.github.takemikami.intellij.plugin.externallinters.LinterProblem;
import com.github.takemikami.intellij.plugin.externallinters.TextOffsetDetector;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MypyLocalInspection extends LocalInspectionTool {
    private static final Logger LOG = Logger.getInstance(MypyLocalInspection.class);

    Pattern OUTPUT_PATTERN = Pattern.compile("([^:]*):([^:]*):([^:]*)\\s*([^:]*):\\s*(.*)");

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);
                Sdk sdk = PythonInspectionUtil.getSdkFromFile(file);
                final String mypybin = PythonInspectionUtil.getPythonCommandBin(sdk, "mypy");
                if (mypybin == null) {
                    return;
                }

                String basePath = PythonInspectionUtil.getBasePathFromFile(file);
                String target = file.getVirtualFile().getCanonicalPath();
                if (target != null) {
                    String body = file.getText();
                    TextOffsetDetector detector = new TextOffsetDetector(body);
                    Path tmp = null;
                    try {
                        tmp = Files.createTempFile(file.getVirtualFile().getNameWithoutExtension(), ".py");
                        PrintWriter pw = new PrintWriter(new FileWriter(tmp.toFile()));
                        pw.write(body);
                        pw.flush();
                        pw.close();
                        String[] cmd = new String[]{
                                mypybin,
                                "--show-column-numbers",
                                "--shadow-file",
                                target, tmp.toFile().getAbsolutePath(), target};
                        String output = CommandUtil.runCommand(cmd, basePath, null);
                        Arrays.stream(output.split("\n")).map(ln -> {
                            Matcher m = OUTPUT_PATTERN.matcher(ln);
                            if (!m.matches()) return null;
                            return new LinterProblem(
                                    m.group(1),
                                    Integer.parseInt(m.group(2)),
                                    Integer.parseInt(m.group(3)),
                                    null,
                                    m.group(4),
                                    m.group(5)
                            );
                        }).filter(Objects::nonNull).forEach(problem -> {
                            int offset = detector.getOffset(problem.lineno, problem.colno);
                            holder.registerProblem(
                                    file,
                                    new TextRange(offset, offset + 1),
                                    "mypy: " + problem.getErrorLevel() + " " + problem.message,
                                    LocalQuickFix.EMPTY_ARRAY);
                        });
                    } catch (IOException ex) {
                        LOG.error("flake8 execution error, ", ex);
                    } finally {
                        if (tmp != null) {
                            try {
                                Files.deleteIfExists(tmp.toAbsolutePath());
                            } catch (IOException ex) {
                                LOG.error("temporary file delete failed, ", ex);
                            }
                        }
                    }
                }
            }
        };
    }
}
