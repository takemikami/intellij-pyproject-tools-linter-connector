package com.github.takemikami.intellij.plugin.externallinters.python;


import com.github.takemikami.intellij.plugin.externallinters.CommandUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.DocumentRunnable;
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
import com.intellij.openapi.command.CommandProcessor;

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

        if (!body.equals(newBody)) {
          final String output = newBody;
          final int bodyLength = body.length();

          Application application = ApplicationManager.getApplication();
          application.runWriteAction(new DocumentRunnable(d, null) {
            @Override
            public void run() {
              CommandProcessor.getInstance().runUndoTransparentAction(new Runnable(){
                @Override
                public void run() {
                  d.replaceString(0, bodyLength, output);
                }
              });
            }
          });
        }

      } catch (IOException e) {
        System.out.println("Error: " + e.getMessage());
      }

    });

  }

}
