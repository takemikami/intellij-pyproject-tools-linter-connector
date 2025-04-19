package com.github.takemikami.intellij.plugin.externallinters.python;

import com.github.takemikami.intellij.plugin.externallinters.LinterProblem;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Local Inspection of flake8.
 */
public class Flake8LocalInspection extends AbstractPythonInspection {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return buildPsiElementVisitorByCommand(holder, isOnTheFly, "pflake8");
  }

  boolean isEnabledByPyprojectToml(String tomlBody) {
    return Arrays.stream(tomlBody.split("\n"))
        .anyMatch(ln -> ln.trim().startsWith("[tool.flake8]"));
  }

  private static final Pattern OUTPUT_PATTERN = Pattern.compile(
      "([^:]*):([^:]*):([^:]*):\\s*([A-Z0-9]*)\\s*(.*)");

  List<LinterProblem> run(String binPath, String basePath, String path, String body) throws IOException {
    return runLinter(
        binPath,
        new String[]{
            "--format",
            "'%(path)s:%(row)d:%(col)d: %(code)s %(text)s'",
            "-"
        },
        basePath,
        null,
        path,
        body,
        OUTPUT_PATTERN
    );
  }

  LinterProblem createLinterProblemByMatcher(@NotNull Matcher matcher) {
    return new LinterProblem(
        matcher.group(1),
        Integer.parseInt(matcher.group(2)),
        Integer.parseInt(matcher.group(3)),
        matcher.group(4),
        null,
        matcher.group(5)
    );
  }

}
