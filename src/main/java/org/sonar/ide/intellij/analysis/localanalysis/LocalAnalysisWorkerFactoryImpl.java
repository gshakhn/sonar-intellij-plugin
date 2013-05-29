package org.sonar.ide.intellij.analysis.localanalysis;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.listener.JobDoneListener;
import org.sonar.ide.intellij.runner.SonarRunnerConsoleMonitor;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 3/4/13
 * Time: 9:50 PM
 * To change this template use File | Settings | File Templates.
 */
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
