package org.sonar.ide.intellij.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.wsclient.services.Server;


public interface IntelliJSonarPluginCore {

    public SonarModuleComponent getSonarModuleComponent(Project project, VirtualFile file);

    public Server getSonarServer(Project project);


    boolean isSonarModuleConfigured(Project project, VirtualFile virtualFile);
}
