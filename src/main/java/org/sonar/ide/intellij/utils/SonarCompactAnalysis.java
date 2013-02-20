package org.sonar.ide.intellij.utils;

import com.intellij.openapi.project.Project;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 2/18/13
 * Time: 10:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SonarCompactAnalysis extends SonarAnalysisAdaptor {

    private SonarAnalysis remoteAnalysis;

    public SonarCompactAnalysis(Project project) {
        super(new SonarRemoteAnalysis(project));

        this.remoteAnalysis = this.sonarAnalysis;
    }

    public void switchToLocalAnalysis(SonarAnalysis localAnalysis) {
        this.sonarAnalysis = localAnalysis;
    }

    public void switchToRemoteAnalysis() {
        this.sonarAnalysis = this.remoteAnalysis;
    }
}
