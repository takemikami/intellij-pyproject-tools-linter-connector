package com.github.takemikami.intellij.plugin.externallinters.python;

import com.github.takemikami.intellij.plugin.externallinters.LinterProblem;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElementVisitor;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Local Inspection of mypy.
 */
public class MypyLocalInspection extends AbstractPythonInspection {

  private static final Logger LOG = Logger.getInstance(MypyLocalInspection.class);

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return buildPsiElementVisitorByCommand(holder, isOnTheFly, "mypy");
  }

  @Override
  boolean isEnabledByPyprojectToml(String tomlBody) {
    return Arrays.stream(tomlBody.split("\n"))
        .anyMatch(ln -> ln.trim().startsWith("[tool.mypy]"));
  }

  private static final Pattern OUTPUT_PATTERN = Pattern.compile(
      "([^:]*):([^:]*):([^:]*):([^:]*):([^:]*):\\s*([^:]*):\\s*(.*)");

  @Override
  List<LinterProblem> run(String binPath, String basePath, String path, String body) throws IOException {
    Path tmp = null;
    try {
      tmp = Files.createTempFile(path.substring(0, path.length() - 3), ".py");
      PrintWriter pw = new PrintWriter(new FileWriter(tmp.toFile()));
      pw.write(body);
      pw.flush();
      pw.close();

      return runLinter(
          binPath,
          new String[]{
              "--show-column-numbers",
              "--show-error-end",
              "--no-color-output",
              "--shadow-file",
              path,
              tmp.toFile().getAbsolutePath(),
              path
          },
          basePath,
          null,
          path,
          null,
          OUTPUT_PATTERN
      );
    } finally {
      if (tmp != null) {
        try {
          Files.deleteIfExists(tmp.toAbsolutePath());
        } catch (IOException ex) {
          // nothing to do
        }
      }
    }
  }

  @Override
  LinterProblem createLinterProblemByMatcher(@NotNull Matcher m) {
    return new LinterProblem(
        m.group(1),
        Integer.parseInt(m.group(2)),
        Integer.parseInt(m.group(3)),
        Integer.parseInt(m.group(4)),
        Integer.parseInt(m.group(5)),
        null,
        m.group(6),
        m.group(7)
    );
  }

  @Override
  String createTargetByMatcher(Matcher m) {
    return m.group(1);
  }

}
