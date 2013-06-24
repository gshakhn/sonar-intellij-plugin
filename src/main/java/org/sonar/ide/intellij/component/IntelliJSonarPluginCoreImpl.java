package org.sonar.ide.intellij.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.utils.SonarResourceKeyUtils;
import org.sonar.wsclient.services.Server;
import org.sonar.wsclient.services.ServerQuery;


public class IntelliJSonarPluginCoreImpl implements IntelliJSonarPluginCore {

    private static IntelliJSonarPluginCore PLUGIN_CORE = new IntelliJSonarPluginCoreImpl();

    public static IntelliJSonarPluginCore getInstance() {
        return PLUGIN_CORE;
    }

    public static void register(IntelliJSonarPluginCore pluginCore) {
        PLUGIN_CORE = pluginCore;
    }

    public SonarModuleComponent getSonarModuleComponent(Project project, VirtualFile file) {
        return SonarResourceKeyUtils.getSonarModuleComponent(project, file);
    }

    public Server getSonarServer(Project project) {
        SonarProjectComponent sonarProjectComponent = project.getComponent(SonarProjectComponent.class);
        return sonarProjectComponent.getSonar().find(new ServerQuery());
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
