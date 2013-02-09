package org.sonar.ide.intellij.worker;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
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

  protected String getResourceKey() {
      return SonarResourceKeyUtils.createFileResourceKey(this.project, virtualFile);
  }

  private SonarModuleComponent getSonarModuleComponent() {
    return SonarResourceKeyUtils.getSonarModuleComponent(project, virtualFile);
  }

    protected Project getProject() {
        return project;
    }
}
