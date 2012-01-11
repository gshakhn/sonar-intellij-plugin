package org.sonar.ide.intellij.component;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import org.sonar.ide.intellij.listener.SonarFileEditorManagerListener;
import org.sonar.ide.intellij.model.ToolWindowModel;

public class SonarProjectComponentImpl implements SonarProjectComponent, ProjectComponent {
  private ToolWindowModel toolWindowModel;
  private Project project;

  public SonarProjectComponentImpl(Project project) {
    this.project = project;
  }

  @Override
  public void projectOpened() {
    project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new SonarFileEditorManagerListener(this));
  }

  @Override
  public void projectClosed() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void initComponent() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void disposeComponent() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "Sonar";
  }

  @Override
  public ToolWindowModel getToolWindowModel() {
    return toolWindowModel;
  }

  @Override
  public void setToolWindowModel(ToolWindowModel model) {
    this.toolWindowModel = model;
  }
}
