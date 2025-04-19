package com.github.takemikami.intellij.plugin.externallinters.python;

import com.github.takemikami.intellij.plugin.externallinters.CommandUtil;
import com.github.takemikami.intellij.plugin.externallinters.LinterProblem;
import com.github.takemikami.intellij.plugin.externallinters.TextOffsetDetector;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

abstract public class AbstractPythonInspection extends LocalInspectionTool {

  public PsiElementVisitor buildPsiElementVisitorByCommand(@NotNull ProblemsHolder holder, boolean isOnTheFly, String commandName) {

    return new PsiElementVisitor() {
      @Override
      public void visitFile(@NotNull PsiFile file) {
        super.visitFile(file);
        Sdk sdk = PythonInspectionUtil.getSdkFromFile(file);
        final String commandBin = PythonInspectionUtil.getPythonCommandBin(sdk, commandName);
        if (commandBin==null) return;
        if (!Files.exists(Paths.get(commandBin))) return;
        String basePath = PythonInspectionUtil.getBasePathFromFile(file);
        try {
          List<String> lines = Files.readAllLines(
              Path.of(basePath + "/pyproject.toml"),
              StandardCharsets.UTF_8
          );
          if (!isEnabledByPyprojectToml(String.join("\n", lines))) {
            return;
          }
        } catch (IOException e) {
          return;
        }

        String path = Objects.requireNonNull(file.getVirtualFile().getCanonicalPath())
            .substring(basePath.length()+1);
        String body = file.getText();
        TextOffsetDetector detector = new TextOffsetDetector(body);
        try {
          List<LinterProblem> problems = run(commandBin, basePath, path, body);
          problems.forEach(problem -> {
            int offset = detector.getOffset(problem.lineno, problem.colno);
            int offsetEnd = offset + 1;
            if (problem.linenoEnd != -1 && problem.colnoEnd != -1) {
              offsetEnd = detector.getOffset(problem.linenoEnd, problem.colnoEnd) + 1;
            }
            holder.registerProblem(
                file,
                new TextRange(offset, offsetEnd),
                commandName + ": " + problem.errorCode + " " + problem.message,
                LocalQuickFix.EMPTY_ARRAY);
          });
        } catch (IOException ex) {
          // LOG.error("flake8 execution error, ", ex);
        }
      }
    };

  }

  public List<LinterProblem> runLinter(String binPath, String[] args, String workingDir, Map<String, String> env, String path, String body, Pattern outputPattern)
      throws IOException {
    String[] cmd = ArrayUtils.addAll(new String[]{binPath}, args);
    String output = CommandUtil.runCommand(cmd, workingDir, env, body);
    return Arrays.stream(output.split("\n")).map(ln -> {
      Matcher m = outputPattern.matcher(ln);
      if (!m.matches()) return null;
      String problemFile = createTargetByMatcher(m);
      if (problemFile!=null && !path.equals(problemFile)) return null;
      return createLinterProblemByMatcher(m);
    }).filter(Objects::nonNull).toList();
  }

  String createTargetByMatcher(Matcher matcher) {
    return null;
  }
  abstract boolean isEnabledByPyprojectToml(String tomlBody);
  abstract List<LinterProblem> run(String binPath, String basePath, String path, String body) throws IOException;
  abstract LinterProblem createLinterProblemByMatcher(@NotNull Matcher matcher);

}
