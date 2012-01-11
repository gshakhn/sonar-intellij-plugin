package org.sonar.ide.intellij.component;

import org.sonar.ide.intellij.model.ToolWindowModel;

public interface SonarProjectComponent {
  void setToolWindowModel(ToolWindowModel model);
  ToolWindowModel getToolWindowModel();
}
