package org.sonar.ide.intellij.component;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.ModuleComponent;
import org.jetbrains.annotations.NotNull;
import org.sonar.ide.intellij.utils.SonarUtils;
import org.sonar.wsclient.Sonar;

@State(name = "SonarConfiguration", storages = {@Storage(id = "other", file = "$MODULE_FILE$")})
public class SonarModuleComponentImpl implements SonarModuleComponent, ModuleComponent, PersistentStateComponent<SonarModuleComponent.SonarModuleState> {

    private SonarModuleState myState = new SonarModuleState();

    public SonarModuleComponentImpl() {
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "SonarModuleComponentImpl";
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        // called when project is being closed
    }

    public void moduleAdded() {
        // Invoked when the module corresponding to this component instance has been completely
        // loaded and added to the project.
    }

    @Override
    public SonarModuleState getState() {
        return myState;
    }

    @Override
    public void loadState(SonarModuleState state) {
        myState = state;
    }

    @Override
    public Sonar getSonar() {
        Sonar sonarConn = SonarUtils.getSonar(myState.host, myState.user, myState.password, myState.useProxy);
        return sonarConn;
    }

    @Override
    public boolean isConfigured() {
        return myState.configured;
    }
}
