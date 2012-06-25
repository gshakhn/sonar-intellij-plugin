package org.sonar.ide.intellij.component;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sonar.ide.intellij.listener.SonarFileEditorManagerListener;
import org.sonar.ide.intellij.model.ToolWindowModel;

@State(name = "SonarConfiguration", storages = {@Storage(id = "other", file = "$PROJECT_FILE$")})
public class SonarProjectComponentImpl implements SonarProjectComponent, ProjectComponent, PersistentStateComponent<SonarProjectComponent.SonarProjectState> {
  private ToolWindowModel toolWindowModel;
  private SonarProjectState state;
  private Project project;

  public SonarProjectComponentImpl(Project project) {
    this.project = project;
    this.state = new SonarProjectState();
  }

  @Override
  public void projectOpened() {
    project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new SonarFileEditorManagerListener(this));
  }

  @Override
  public void projectClosed() {
  }

  @Override
  public void initComponent() {
  }

  @Override
  public void disposeComponent() {
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
  public SonarProjectState getState() {
    return state;
  }

  @Override
  public void loadState(SonarProjectState state) {
    this.state = state;
  }

  @Override
  public void setToolWindowModel(ToolWindowModel model) {
    this.toolWindowModel = model;
  }
}
