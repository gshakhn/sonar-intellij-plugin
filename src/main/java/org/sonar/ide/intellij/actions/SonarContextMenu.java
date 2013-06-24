package org.sonar.ide.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.component.IntelliJSonarPluginCoreImpl;


public class SonarContextMenu extends DefaultActionGroup {

    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);

        if ((project == null || project.isDefault()) || file == null || !IntelliJSonarPluginCoreImpl.getInstance().isSonarModuleConfigured(project, file)) {
            e.getPresentation().setEnabled(false);
            e.getPresentation().setVisible(false);
        }

    }
}