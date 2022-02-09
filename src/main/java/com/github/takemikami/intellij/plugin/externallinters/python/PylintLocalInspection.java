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

/**
 * Local Inspection of pylint.
 */
public class PylintLocalInspection extends LocalInspectionTool {

  private static final Logger LOG = Logger.getInstance(PylintLocalInspection.class);

  private static final Pattern OUTPUT_PATTERN = Pattern.compile(
      "([^:]*):([^:]*):([^:]*):\\s*([A-Z0-9]*):\\s*(.*)");

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitFile(@NotNull PsiFile file) {
        super.visitFile(file);
        Sdk sdk = PythonInspectionUtil.getSdkFromFile(file);
        final String pylintbin = PythonInspectionUtil.getPythonCommandBin(sdk, "pylint");
        if (pylintbin == null) {
          return;
        }

        String basePath = PythonInspectionUtil.getBasePathFromFile(file);
        String body = file.getText();
        TextOffsetDetector detector = new TextOffsetDetector(body);
        try {
          String[] cmd = new String[]{pylintbin, "--from-stdin", file.getName()};
          String output = CommandUtil.runCommand(cmd, basePath, null, body);
          Arrays.stream(output.split("\n")).map(ln -> {
            Matcher m = OUTPUT_PATTERN.matcher(ln);
            if (!m.matches()) {
              return null;
            }
            return new LinterProblem(
                m.group(1),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)) + 1,
                m.group(4),
                null,
                m.group(5)
            );
          }).filter(Objects::nonNull).forEach(problem -> {
            int offset = detector.getOffset(problem.lineno, problem.colno);
            holder.registerProblem(
                file,
                new TextRange(offset, offset + 1),
                "pylint: " + problem.errorCode + " " + problem.message,
                LocalQuickFix.EMPTY_ARRAY);
          });
        } catch (IOException ex) {
          LOG.error("pylint execution error, ", ex);
        }
      }
    };
  }
}
