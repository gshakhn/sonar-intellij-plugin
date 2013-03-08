package org.sonar.ide.intellij.inspection;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.sonar.ide.intellij.component.SonarProjectComponent;
import org.sonar.wsclient.services.Violation;

public class MarkFixedQuickFix extends LocalQuickFixBase {

    private Violation violation;

    protected MarkFixedQuickFix(Violation violation) {
        super("Mark fixed", "Sonar");
        this.violation = violation;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        SonarProjectComponent component = project.getComponent(SonarProjectComponent.class);
        VirtualFile virtualFile = descriptor.getPsiElement().getContainingFile().getVirtualFile();
        component.getSonarCache().removeViolation(virtualFile, this.violation);
        component.getToolWindowModel().refreshViolationsTable(virtualFile);
    }
}
