package org.sonar.ide.intellij.worker;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.ide.intellij.listener.JobDoneListener;
import org.sonar.ide.intellij.runner.SonarRunner;
import org.sonar.ide.intellij.runner.SonarRunnerJavaParameters;
import org.sonar.ide.intellij.runner.SonarRunnerMonitor;
import org.sonar.ide.intellij.utils.SonarLocalAnalysisAnalysis;
import org.sonar.ide.intellij.utils.SonarResourceKeyUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutionException;

/**
 * Worker to handle the local sonar analysis
 */
public class LocalAnalysisWorker extends SwingWorker<SonarLocalAnalysisAnalysis, Void> {

    private Project project;

    private VirtualFile file;

    private SonarRunnerMonitor sonarRunnerMonitor;

    private JobDoneListener<SonarLocalAnalysisAnalysis> listener;

    public LocalAnalysisWorker(Project project, VirtualFile file, SonarRunnerMonitor sonarRunnerMonitor, JobDoneListener<SonarLocalAnalysisAnalysis> listener) {
        this.project = project;
        this.file = file;
        this.sonarRunnerMonitor = sonarRunnerMonitor;
        this.listener = listener;
    }

    @Override
    protected SonarLocalAnalysisAnalysis doInBackground() throws Exception {

        SonarRunnerJavaParameters javaParameters = new SonarRunnerJavaParameters();
        SonarModuleComponent.SonarModuleState state = SonarResourceKeyUtils.getSonarModuleComponent(project, file).getState();

        javaParameters.withProjectKey(state.projectKey).
                withProjectName(project.getName()).
                withProjectVersion("1").runOnSource("src").
                withUserName(state.user).
                withPassword(state.password).
                withHost(state.host);

        final File result = new SonarRunner(sonarRunnerMonitor).
                runAnalysis(project, javaParameters);

        sonarRunnerMonitor.dumpLogs(System.out); // TODO(AGAL) : remove this

        Object obj = JSONValue.parse(new FileReader(result));
        JSONObject sonarResult = (JSONObject) obj;

        // it is better yet not to read all
        final JSONObject violationByResources = (JSONObject) sonarResult.get("violations_per_resource");


        return new SonarLocalAnalysisAnalysis(project, violationByResources);
    }

    @Override
    protected void done() {
        try {
            listener.jobDone(get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
