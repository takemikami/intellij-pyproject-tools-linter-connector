package com.github.takemikami.intellij.plugin.externallinters.python;

import com.github.takemikami.intellij.plugin.externallinters.*;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Flake8LocalInspection extends LocalInspectionTool {
    private static final Logger LOG = Logger.getInstance(Flake8LocalInspection.class);

    Pattern OUTPUT_PATTERN = Pattern.compile("([^:]*):([^:]*):([^:]*):\\s*([A-Z0-9]*)\\s*(.*)");

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {

        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);
                Sdk sdk = PythonInspectionUtil.getSdkFromFile(file);
                final String flake8bin = PythonInspectionUtil.getPythonCommandBin(sdk, "flake8");
                System.out.println("flake8bin: " + flake8bin);
                if (flake8bin == null) {
                    return;
                }

                String basePath = PythonInspectionUtil.getBasePathFromFile(file);
                String target = file.getVirtualFile().getCanonicalPath();
                if (target != null) {
                    String body = file.getText();
                    TextOffsetDetector detector = new TextOffsetDetector(body);
                    try {
                        String[] cmd = new String[]{flake8bin, "-"};
                        String output = CommandUtil.runCommand(cmd, basePath, null, body);
                        Arrays.stream(output.split("\n")).map(ln -> {
                            Matcher m = OUTPUT_PATTERN.matcher(ln);
                            if (!m.matches()) return null;
                            return new LinterProblem(
                                    m.group(1),
                                    Integer.parseInt(m.group(2)),
                                    Integer.parseInt(m.group(3)),
                                    m.group(4),
                                    null,
                                    m.group(5)
                            );
                        }).filter(Objects::nonNull).forEach(problem -> {
                            int offset = detector.getOffset(problem.lineno, problem.colno);
                            holder.registerProblem(
                                    file,
                                    new TextRange(offset, offset + 1),
                                    "flake8: " + problem.errorCode + " " + problem.message,
                                    LocalQuickFix.EMPTY_ARRAY);
                        });
                    } catch (IOException ex) {
                        LOG.error("flake8 execution error, ", ex);
                    }
                }
            }
        };
    }
}


