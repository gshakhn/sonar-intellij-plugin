package org.sonar.ide.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.sonar.ide.intellij.component.SonarProjectComponent;

public class ClearSonarCache extends DumbAwareAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    final Project project = anActionEvent.getData(PlatformDataKeys.PROJECT);
    project.getComponent(SonarProjectComponent.class).getSonarCache().clearCache();
  }
}
