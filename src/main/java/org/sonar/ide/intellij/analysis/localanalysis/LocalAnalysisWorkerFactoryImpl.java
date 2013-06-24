package org.sonar.ide.intellij.analysis.localanalysis;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.JobDoneListener;
import org.sonar.ide.intellij.runner.SonarRunnerConsoleMonitor;


public class LocalAnalysisWorkerFactoryImpl implements LocalAnalysisWorkerFactory {

    private static LocalAnalysisWorkerFactory Instance = new LocalAnalysisWorkerFactoryImpl();


    public static void register(LocalAnalysisWorkerFactory factory) {
        Instance = factory;
    }

    public static LocalAnalysisWorkerFactory getInstance() {
        return Instance;
    }

    public LocalAnalysisWorker createLocalAnalysisWorker(Project project, VirtualFile file, SonarRunnerConsoleMonitor sonarRunnerMonitor, JobDoneListener<RunResult> jobDoneListener) {
        return new LocalAnalysisWorker(project, file, sonarRunnerMonitor, jobDoneListener);
    }
}
