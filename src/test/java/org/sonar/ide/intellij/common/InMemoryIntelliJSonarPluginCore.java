package org.sonar.ide.intellij.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.component.IntelliJSonarPluginCore;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.wsclient.services.Server;


public class InMemoryIntelliJSonarPluginCore implements IntelliJSonarPluginCore {

    private Server sonarServer = new Server();

    public InMemoryIntelliJSonarPluginCore() {
        this.sonarServer.setVersion("3.4");
    }

    @Override
    public SonarModuleComponent getSonarModuleComponent(Project project, VirtualFile file) {
        return project.getComponent(SonarModuleComponent.class);
    }

    @Override
    public Server getSonarServer(Project project) {
        return sonarServer;
    }

    @Override
    public boolean isSonarModuleConfigured(Project project, VirtualFile file) {

        if (project == null || file == null) {
            return false;
        }
        SonarModuleComponent sonarModuleComponent = getSonarModuleComponent(project, file);

        return sonarModuleComponent != null && sonarModuleComponent.isConfigured();
    }


}
