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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    final Map<VirtualFile, List<Violation>> violations = this.violations;
    List<ProblemDescriptor> problems = ApplicationManager.getApplication().runReadAction(new Computable<List<ProblemDescriptor>>() {
      @Override
      public List<ProblemDescriptor> compute() {
        RefreshViolationsWorker refreshViolationsWorker = new RefreshViolationsWorker(file.getProject(), file.getVirtualFile());
        refreshViolationsWorker.execute();
        try {
          List<Violation> violationList = refreshViolationsWorker.get(5, TimeUnit.SECONDS);
          if (!isOnTheFly && violationList != null)
            violations.put(file.getVirtualFile(), violationList);
          return buildProblemDescriptors(
              violationList,
              manager,
              file,
              isOnTheFly
          );
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        } catch (TimeoutException e) {
          e.printStackTrace();
          ProblemDescriptor timeoutDescriptor = manager.createProblemDescriptor(
              file,
              "Timeout",
              LocalQuickFix.EMPTY_ARRAY,
              ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
              isOnTheFly,
              false
          );
          List<ProblemDescriptor> descriptors = new LinkedList<ProblemDescriptor>();
          descriptors.add(timeoutDescriptor);
          return descriptors;
        }
        return null;
      }
    });
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
