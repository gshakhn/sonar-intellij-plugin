package org.sonar.ide.intellij.analysis.localanalysis;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.JobDoneListener;
import org.sonar.ide.intellij.runner.SonarRunnerConsoleMonitor;


public interface LocalAnalysisWorkerFactory {
    public LocalAnalysisWorker createLocalAnalysisWorker(Project project, VirtualFile file, SonarRunnerConsoleMonitor sonarRunnerMonitor, JobDoneListener<RunResult> jobDoneListener);
}
