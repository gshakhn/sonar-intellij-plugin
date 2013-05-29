package org.sonar.ide.intellij.component;

import org.sonar.ide.intellij.analysis.SonarAnalysis;
import org.sonar.ide.intellij.model.ToolWindowModel;
import org.sonar.wsclient.Sonar;

public interface SonarProjectComponent {
    SonarProjectState getState();


    class SonarProjectState {
        public String host;
        public String user;
        public String password;
        public boolean assignProject;
        public boolean configured;
        public boolean useProxy;
    }

    void setToolWindowModel(ToolWindowModel model);

    ToolWindowModel getToolWindowModel();

    Sonar getSonar();

    SonarAnalysis getSonarAnalysis();

    void switchToLocalAnalysis(SonarAnalysis result);

    void switchToExistingLocalAnalysis();

    void switchToRemoteAnalysis();

    boolean isLocalAnalysisAvailable();
}
