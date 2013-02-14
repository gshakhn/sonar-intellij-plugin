package org.sonar.ide.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.ide.intellij.worker.ResourceLookupWorker;
import org.sonar.ide.intellij.utils.SonarResourceKeyUtils;
import org.sonar.wsclient.services.Resource;


public class ShowSonarAsWebbView extends DumbAwareAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getData(PlatformDataKeys.PROJECT);
        if (project == null || project.isDefault()) {
            return;
        }

        final VirtualFile file = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
         if(file == null) {
             return;
         }

        ResourceLookupWorker resourceLookupWorker = new ResourceLookupWorker(project, file, new ResourceLookupWorker.ResourceLoadCallback() {
            @Override
            public void resourceLoaded(@Nullable Resource resource) {

                SonarModuleComponent sonarModuleComponent = SonarResourceKeyUtils.getSonarModuleComponent(project, file);
                SonarModuleComponent.SonarModuleState state = sonarModuleComponent.getState();

                String resourceId;
                if (resource == null) {
                    resourceId = state.projectKey;
                } else {
                    resourceId = Integer.toString(resource.getId());
                }

                new SonarNavigator().navigateToDashboard(sonarModuleComponent, resourceId);
            }
        });
        resourceLookupWorker.execute();

    }
}
