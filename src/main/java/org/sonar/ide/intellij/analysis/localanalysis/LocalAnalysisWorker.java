package org.sonar.ide.intellij.analysis.localanalysis;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.ide.intellij.listener.JobDoneListener;
import org.sonar.ide.intellij.runner.SonarRunner;
import org.sonar.ide.intellij.runner.SonarRunnerConsoleMonitor;
import org.sonar.ide.intellij.runner.SonarRunnerJavaParameters;
import org.sonar.ide.intellij.utils.SonarResourceKeyUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutionException;

/**
 * Worker to handle the local sonar analysis
 */
public class LocalAnalysisWorker extends SwingWorker<RunResult, Void> {

    private Project project;

    private VirtualFile scopeFile;

    private SonarRunnerConsoleMonitor sonarRunnerMonitor;

    private JobDoneListener<RunResult> listener;

    public LocalAnalysisWorker(Project project, VirtualFile scopeFile, SonarRunnerConsoleMonitor sonarRunnerMonitor, JobDoneListener<RunResult> listener) {
        this.project = project;
        this.scopeFile = scopeFile;
        this.sonarRunnerMonitor = sonarRunnerMonitor;
        this.listener = listener;
    }

    @Override
    protected RunResult doInBackground() throws Exception {

        SonarRunnerJavaParameters javaParameters = new SonarRunnerJavaParameters();
        SonarModuleComponent.SonarModuleState state = SonarResourceKeyUtils.getSonarModuleComponent(project, scopeFile).getState();

        Module moduleForFile = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(scopeFile);

        if (moduleForFile == null) {
            return new RunResult("Selected file doesn't belong to a module", 1);
        }
        VirtualFile[] contentSourceRoots = ModuleRootManager.getInstance(moduleForFile).getSourceRoots();

        if (contentSourceRoots.length == 0) {
            return new RunResult("No content source root is defined", 1);
        }

        javaParameters.withProjectKey(state.projectKey).
                withProjectName(project.getName()).
                withProjectVersion("1").runOnSource(join(contentSourceRoots)).
                withUserName(state.user).
                withPassword(state.password).
                withHost(state.host);

        final File result;
        try {
            result = new SonarRunner(sonarRunnerMonitor).runAnalysis(project, javaParameters);
        } catch (Exception e) {
            e.printStackTrace();
            return new RunResult("Error running local analysis", e);
        }

        if (sonarRunnerMonitor.getExitCode() != 0) {
            return new RunResult("Error running local analysis check log file", sonarRunnerMonitor.getExitCode());
        } else {
            Object obj = JSONValue.parse(new FileReader(result));
            JSONObject sonarResult = (JSONObject) obj;

            // it is better yet not to read all
            final JSONObject violationByResources = (JSONObject) sonarResult.get("violations_per_resource");


            return new RunResult(new SonarLocalAnalysis(project, violationByResources), sonarRunnerMonitor.getExitCode());
        }
    }

    private String join(VirtualFile[] contentSourceRoots) {
        final StringBuilder builder = new StringBuilder();
        for (VirtualFile contentSourceRoot : contentSourceRoots) {
            builder.append(contentSourceRoot.getPath()).append(",");
        }

        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
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
