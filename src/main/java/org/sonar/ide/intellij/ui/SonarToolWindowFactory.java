package org.sonar.ide.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

public class SonarToolWindowFactory implements ToolWindowFactory {
  public SonarToolWindowFactory() {
  }

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    SonarToolWindow.createSonarToolWindow(project, toolWindow);
  }
}
