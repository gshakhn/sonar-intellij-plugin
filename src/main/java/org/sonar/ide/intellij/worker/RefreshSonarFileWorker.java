package org.sonar.ide.intellij.worker;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Model;
import org.sonar.wsclient.services.Query;

import javax.swing.*;
import java.util.List;

public abstract class RefreshSonarFileWorker<T extends Model> extends SwingWorker<List<T>, Void> {
  private Project project;
  protected VirtualFile virtualFile;

  protected RefreshSonarFileWorker(Project project, VirtualFile virtualFile) {
    this.project = project;
    this.virtualFile = virtualFile;
  }

  @Override
  protected List<T> doInBackground() throws Exception {
    String resourceKey = getResourceKey();
    if (resourceKey == null) {
      return null;
    }
    Sonar sonar = getSonar();

    Query<T> query = getQuery(resourceKey);
    return sonar.findAll(query);
  }
  
  protected abstract Query<T> getQuery(String resourceKey);

  private Sonar getSonar() {
    return getSonarModuleComponent().getSonar();
  }

  private String getResourceKey() {
    final SonarModuleComponent sonarModuleComponent = getSonarModuleComponent();
    if (sonarModuleComponent == null) { // There is no module for this file
      return null;
    }

    if (!sonarModuleComponent.isConfigured()) {
      return null;
    }

    final PsiManager psiManager = PsiManager.getInstance(this.project);
    final PsiFile psiFile = ApplicationManager.getApplication().runReadAction(new Computable<PsiFile>() {
      @Override
      public PsiFile compute() {
        return psiManager.findFile(virtualFile);
      }
    });

    if (!(psiFile instanceof PsiJavaFile)) {
      return null;
    }

    return ApplicationManager.getApplication().runReadAction(new Computable<String>() {
      @Override
      public String compute() {
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        String packageName = psiJavaFile.getPackageName();
        String className = psiJavaFile.getClasses()[0].getName();

        return sonarModuleComponent.getState().projectKey + ":" + packageName + "." + className;
      }
    });
  }

  private SonarModuleComponent getSonarModuleComponent() {
    Module module = ModuleUtil.findModuleForFile(this.virtualFile, this.project);
    if (module == null) { // This file doesn't belong to a module.
      return null;
    } else {
      return module.getComponent(SonarModuleComponent.class);
    }
  }
}
