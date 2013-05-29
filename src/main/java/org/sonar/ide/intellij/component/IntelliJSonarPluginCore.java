package org.sonar.ide.intellij.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.wsclient.services.Server;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 3/7/13
 * Time: 11:10 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IntelliJSonarPluginCore {

    public SonarModuleComponent getSonarModuleComponent(Project project, VirtualFile file);

    public Server getSonarServer(Project project);


    boolean isSonarModuleConfigured(Project project, VirtualFile virtualFile);
}
