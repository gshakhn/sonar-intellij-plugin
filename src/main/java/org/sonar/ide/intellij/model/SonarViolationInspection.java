package org.sonar.ide.intellij.model;

import com.intellij.codeInspection.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.ide.intellij.worker.RefreshViolationsWorker;
import org.sonar.wsclient.services.Violation;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class SonarViolationInspection extends AbstractSonarInspection {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Violations";
  }

  @NotNull
  @Override
  public String getShortName() {
    return "Sonar.Violations";
  }

  private Map<VirtualFile, List<Violation>> violations = new ConcurrentHashMap<VirtualFile, List<Violation>>();
  private boolean isOnTheFly = true;

  @Nullable
  public ProblemDescriptor[] checkFile(final @NotNull PsiFile file, final @NotNull InspectionManager manager, final boolean isOnTheFly) {
    List<Violation> violationList = ApplicationManager.getApplication().runReadAction(new Computable<List<Violation>>() {
      @Override
      public List<Violation> compute() {
        RefreshViolationsWorker refreshViolationsWorker = new RefreshViolationsWorker(file.getProject(), file.getVirtualFile());
        refreshViolationsWorker.execute();
        try {
          return refreshViolationsWorker.get();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
        return null;
      }
    });
    if (!isOnTheFly && violationList != null)
      violations.put(file.getVirtualFile(), violationList);
    List<ProblemDescriptor> problems = buildProblemDescriptors(
        violationList,
        manager,
        file,
        isOnTheFly
    );

    if (problems == null) {
      return null;
    }
    return problems.toArray(new ProblemDescriptor[problems.size()]);
  }

  @Nullable
  private List<ProblemDescriptor> buildProblemDescriptors(
      @Nullable List<Violation> violations,
      @NotNull InspectionManager manager,
      PsiElement element,
      boolean isOnTheFly
  ) {
    if (violations == null) {
      return null;
    }

    Project project = element.getProject();
    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
    Document document = documentManager.getDocument(element.getContainingFile());
    if (document == null) {
      return null;
    }

    List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
    for (Violation violation : violations) {
      Integer line = violation.getLine();
      if (line != null) {
        ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
            element,
            getTextRange(document, line - 1),
            violation.getMessage(),
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            isOnTheFly,
            LocalQuickFix.EMPTY_ARRAY
        );
        problems.add(problemDescriptor);
      }
    }
    return problems;
  }

  @Override
  public void inspectionStarted(LocalInspectionToolSession session, boolean isOnTheFly) {
    this.isOnTheFly = isOnTheFly;
  }

  @Override
  public void inspectionFinished(LocalInspectionToolSession session, ProblemsHolder problemsHolder) {
    if (!isOnTheFly)
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          LastInspectionResult.getInstance().setViolations(violations);
        }
      });
    super.inspectionFinished(session, problemsHolder);
  }
}
