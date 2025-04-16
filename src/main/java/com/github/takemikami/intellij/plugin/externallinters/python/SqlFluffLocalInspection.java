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
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

class SqlFluffLocalInspection extends LocalInspectionTool {

  private static final Logger LOG = Logger.getInstance(SqlFluffLocalInspection.class);

  private static final Pattern OUTPUT_PATTERN = Pattern.compile(
      "L:\\s*([0-9]+)\\s*\\|\\s*P:\\s*([0-9]+)\\s*\\|\\s*([A-Z0-9]+)\\s*\\|\\s(.*)");

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {

    return new PsiElementVisitor() {
      @Override
      public void visitFile(@NotNull PsiFile file) {
        super.visitFile(file);
        Sdk sdk = PythonInspectionUtil.getSdkFromFile(file);
        final String sqlfluff8bin = PythonInspectionUtil.getPythonCommandBin(sdk, "sqlfluff");
        if (sqlfluff8bin == null) {
          return;
        }

        String basePath = PythonInspectionUtil.getBasePathFromFile(file);
        String body = file.getText();
        TextOffsetDetector detector = new TextOffsetDetector(body);
        try {
          String[] cmd = new String[]{sqlfluff8bin, "lint", "-"};
          String output = CommandUtil.runCommand(cmd, basePath, null, body);
          Arrays.stream(output.split("\n")).map(ln -> {
            Matcher m = OUTPUT_PATTERN.matcher(ln);
            if (!m.matches()) {
              return null;
            }
            return new LinterProblem(
                null,
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                m.group(3),
                null,
                m.group(4)
            );
          }).filter(Objects::nonNull).forEach(problem -> {
            int offset = detector.getOffset(problem.lineno, problem.colno);
            holder.registerProblem(
                file,
                new TextRange(offset, offset + 1),
                "sqlfluff: " + problem.errorCode + " " + problem.message,
                LocalQuickFix.EMPTY_ARRAY);
          });
        } catch (IOException ex) {
          LOG.error("sqlfluff execution error, ", ex);
        }
      }
    };
  }

}
