package org.sonar.ide.intellij.model;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.sonar.ide.intellij.SonarModuleComponent;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Source;
import org.sonar.wsclient.services.SourceQuery;
import org.sonar.wsclient.services.Violation;
import org.sonar.wsclient.services.ViolationQuery;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ViolationTableModel extends AbstractTableModel {

  private List<Violation> violations = new ArrayList<Violation>();
  private VirtualFile virtualFile;
  private Source sourceCode;

  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "Severity";
      case 1:
        return "Rule Name";
      case 2:
        return "Line Number";
      case 3:
        return "Line";
      case 4:
        return "Message";
      default:
        return "?";
    }
  }

  @Override
  public int getRowCount() {
    return violations.size();
  }

  @Override
  public int getColumnCount() {
    return 5;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Violation violation = violations.get(rowIndex);
    switch (columnIndex) {
      case 0:
        return violation.getSeverity();
      case 1:
        return violation.getRuleName();
      case 2:
        return violation.getLine();
      case 3:
        return sourceCode.getLine(violation.getLine());
      case 4:
        return violation.getMessage();
      default:
        return "";
    }
  }

  public void refreshViolations() {
    DataManager.getInstance().getDataContextFromFocus().doWhenDone(new AsyncResult.Handler<DataContext>() {
      @Override
      public void run(DataContext dataContext) {
        violations.clear();

        Project project = DataKeys.PROJECT.getData(dataContext);
        VirtualFile[] files = FileEditorManager.getInstance(project).getSelectedFiles();
        if (files.length > 0) {
          virtualFile = files[0];
          Module module = ModuleUtil.findModuleForFile(virtualFile, project);
          SonarModuleComponent sonarModuleComponent = module.getComponent(SonarModuleComponent.class);

          PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
          if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            String packageName = psiJavaFile.getPackageName();
            String className = psiJavaFile.getClasses()[0].getName();

            String resourceKey = sonarModuleComponent.getState().projectKey + ":" + packageName + "." + className;

            Sonar sonar = sonarModuleComponent.getSonar();

            ViolationQuery violationQuery = ViolationQuery.createForResource(resourceKey);
            violationQuery.setDepth(-1);
            violations = sonar.findAll(violationQuery);

            SourceQuery sourceQuery = SourceQuery.create(resourceKey);
            sourceCode = sonar.find(sourceQuery);

            fireTableDataChanged();
          }
        }
      }
    });
  }

  public Violation getViolation(int index) {
    return violations.get(index);
  }

  public VirtualFile getVirtualFile() {
    return virtualFile;
  }
}
