package com.github.takemikami.intellij.plugin.externallinters.python;


import com.github.takemikami.intellij.plugin.externallinters.CommandUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import com.intellij.openapi.editor.Document;
import java.util.List;
import java.util.Objects;

public class PythonFormatterDocumentManagerListener implements FileDocumentManagerListener {

  private final Project project;

  public PythonFormatterDocumentManagerListener(final Project project) {
    this.project = project;
  }

  @Override
  public void beforeAllDocumentsSaving() {
    final List<Document> unsavedDocuments = Arrays.asList(FileDocumentManager.getInstance().getUnsavedDocuments());

    if (this.project == null) return;
    PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(this.project);

    unsavedDocuments.forEach(d -> {
      PsiFile file = psiDocumentManager.getPsiFile(d);
      if (file == null) return;
      Sdk sdk = PythonInspectionUtil.getSdkFromFile(file);
      if (sdk == null) return;

      String basePath = PythonInspectionUtil.getBasePathFromFile(file);
      String path = Objects.requireNonNull(file.getVirtualFile().getCanonicalPath())
          .substring(basePath.length()+1);
      String body = file.getText();

      try {
        List<String> lines = Files.readAllLines(
            Path.of(basePath + "/pyproject.toml"),
            StandardCharsets.UTF_8
        );

        String newBody = body;

        // black
        boolean enableBlack = lines.stream().anyMatch(ln -> ln.trim().startsWith("[tool.black]"));
        final String commandBinBlack = PythonInspectionUtil.getPythonCommandBin(sdk, "black");
        if (enableBlack && commandBinBlack!=null) {
          newBody = CommandUtil.runCommand(
              new String[]{
                  commandBinBlack,
                  "-"
              },
              basePath,
              null,
              newBody
          );
        }
        // isort
        boolean enableIsort = lines.stream().anyMatch(ln -> ln.trim().startsWith("[tool.isort]"));
        final String commandBinIsort = PythonInspectionUtil.getPythonCommandBin(sdk, "isort");
        if (enableIsort && commandBinIsort!=null) {
          newBody = CommandUtil.runCommand(
              new String[]{
                  commandBinIsort,
                  "-"
              },
              basePath,
              null,
              newBody
          );
        }

        final String output = newBody;

        Application application = ApplicationManager.getApplication();
        Runnable action = () -> {
          d.setText(output);
        };
        application.runWriteAction(action);

      } catch (IOException e) {
        System.out.println("Error: " + e.getMessage());
      }

    });

  }

}
