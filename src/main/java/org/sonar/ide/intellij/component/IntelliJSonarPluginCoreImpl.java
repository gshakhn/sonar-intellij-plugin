package org.sonar.ide.intellij.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.utils.SonarResourceKeyUtils;
import org.sonar.wsclient.services.Server;
import org.sonar.wsclient.services.ServerQuery;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 3/7/13
 * Time: 11:30 PM
 * To change this template use File | Settings | File Templates.
 */
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
