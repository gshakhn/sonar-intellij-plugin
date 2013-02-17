package org.sonar.ide.intellij.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.sonar.ide.intellij.component.SonarProjectComponent;
import org.sonar.wsclient.services.Violation;

public class MarkFixedQuickFix implements LocalQuickFix {

  private Violation violation;

  protected MarkFixedQuickFix(Violation violation) {
    this.violation = violation;
  }

  @NotNull
  @Override
  public String getName() {
      return "Mark fixed";
  }

  @NotNull
  @Override
  public String getFamilyName() {
      return "Sonar";
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    SonarProjectComponent component = project.getComponent(SonarProjectComponent.class);
    VirtualFile virtualFile = descriptor.getPsiElement().getContainingFile().getVirtualFile();
    component.getSonarCache().removeViolation(virtualFile, this.violation);
    component.getToolWindowModel().refreshViolationsTable(virtualFile);
  }
}
