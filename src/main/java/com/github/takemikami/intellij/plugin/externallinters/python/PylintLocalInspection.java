package com.github.takemikami.intellij.plugin.externallinters.python;

import com.github.takemikami.intellij.plugin.externallinters.LinterProblem;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElementVisitor;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Local Inspection of pylint.
 */
public class PylintLocalInspection extends AbstractPythonInspection {

  private static final Logger LOG = Logger.getInstance(PylintLocalInspection.class);

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return buildPsiElementVisitorByCommand(holder, isOnTheFly, "pylint");
  }

  boolean isEnabledByPyprojectToml(String tomlBody) {
    return Arrays.stream(tomlBody.split("\n"))
        .anyMatch(ln -> ln.trim().startsWith("[tool.pylint."));
  }

  private static final Pattern OUTPUT_PATTERN = Pattern.compile(
      "([^:]*):([^:]*):([^:]*):([^:]*):([^:]*):\\s*([A-Z0-9]*):\\s*(.*)");

  List<LinterProblem> run(String binPath, String basePath, String path, String body) throws IOException {
    return runLinter(
        binPath,
        new String[]{
            "--msg-template='{path}:{line}:{column}:{end_line}:{end_column}: {msg_id}: {msg}'",
            "--from-stdin",
            path
        },
        basePath,
        null,
        path,
        body,
        OUTPUT_PATTERN
    );
  }

  LinterProblem createLinterProblemByMatcher(@NotNull Matcher m) {
    try {
      return new LinterProblem(
          m.group(1),
          Integer.parseInt(m.group(2)),
          Integer.parseInt(m.group(3)) + 1,
          Integer.parseInt(m.group(4)),
          Integer.parseInt(m.group(5)),
          m.group(6),
          null,
          m.group(7)
      );
    } catch (NumberFormatException e) {
      try {
        return new LinterProblem(
            m.group(1),
            Integer.parseInt(m.group(2)),
            Integer.parseInt(m.group(3)) + 1,
            m.group(6),
            null,
            m.group(7)
        );
      } catch (NumberFormatException e2) {
        return null;
      }
    }
  }

}
