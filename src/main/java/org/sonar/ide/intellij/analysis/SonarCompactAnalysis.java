package org.sonar.ide.intellij.analysis;

import com.intellij.openapi.project.Project;


public class SonarCompactAnalysis extends SonarAnalysisAdaptor {

    private SonarAnalysis remoteAnalysis;

    private SonarAnalysis currentLocalAnalysis;

    public SonarCompactAnalysis(Project project) {
        super(new SonarRemoteAnalysis(project));

        this.remoteAnalysis = this.sonarAnalysis;
    }

    public void switchToLocalAnalysis(SonarAnalysis localAnalysis) {
        this.sonarAnalysis = localAnalysis;
        this.currentLocalAnalysis = localAnalysis;
    }

    public void switchToRemoteAnalysis() {
        if (this.remoteAnalysis == null) {
            throw new IllegalStateException("Remote analysis is not available");
        }
        this.sonarAnalysis = this.remoteAnalysis;
    }

    public void switchToCurrentLocalAnalysis() {
        if (this.currentLocalAnalysis == null) {
            throw new IllegalStateException("Local analysis is not available");
        }
        this.sonarAnalysis = this.currentLocalAnalysis;
    }

    public boolean isLocalAnalysisAvailable() {
        return this.currentLocalAnalysis != null;
    }
}
