package org.sonar.ide.intellij.analysis.localanalysis;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.component.EventBus;
import org.sonar.ide.intellij.component.EventKind;
import org.sonar.ide.intellij.component.SonarProjectComponent;
import org.sonar.ide.intellij.listener.JobDoneListener;
import org.sonar.ide.intellij.model.SonarConsole;
import org.sonar.ide.intellij.runner.SonarRunnerConsoleMonitor;

/**
 * Main entry point for doing local analysis
 */
public class SonarLocalAnalyzer {

    private LocalAnalysisWorker localAnalysisWorker;

    public void runLocalAnalysis(final Project project, final VirtualFile file, final SonarRunnerConsoleMonitor sonarRunnerMonitor) {

        checkAnalysisIsRunning();

        synchronized (this) {
            checkAnalysisIsRunning();

            localAnalysisWorker = LocalAnalysisWorkerFactoryImpl.getInstance().createLocalAnalysisWorker(project, file, sonarRunnerMonitor, new JobDoneListener<RunResult>() {
                @Override
                public void jobDone(RunResult result) {
                    SonarProjectComponent sonarProjectComponent = project.getComponent(SonarProjectComponent.class);

                    EventBus.notifyEvent(EventKind.LOCAL_ANALYSIS_FINISHED);

                    if (result.isSuccess()) {
                        logAnalysisEndedWithSuccess(project);
                        sonarProjectComponent.switchToLocalAnalysis(result.getLocalAnalysisAnalysis());
                    } else if (sonarRunnerMonitor.shouldCancelRequest()) {
                        logAnalysisStopped(project);
                    } else {
                        logAnalysisEndedInError(result, project);
                    }

                    localAnalysisWorker = null;
                }
            });
        }

        localAnalysisWorker.execute();
        logAnalysisStarted(project);
    }


    private void checkAnalysisIsRunning() {
        if (isLocalAnalysisRunning()) {
            throw new IllegalStateException("Local analysis is running");
        }
    }

    public boolean isLocalAnalysisRunning() {
        return (localAnalysisWorker != null && !localAnalysisWorker.isDone());
    }

    private void logAnalysisStopped(Project project) {
        Notifications.Bus.notify(new Notification("sonarrunner", "Analysis stopped", "Sonar analysis requested to be stopped", NotificationType.INFORMATION), project);
    }

    private void logAnalysisEndedInError(RunResult result, Project project) {
        Notifications.Bus.notify(new Notification("sonarrunner", "Analysis failed", "Sonar analysis failed check console for more details", NotificationType.ERROR), project);
        SonarConsole.getInstance().addMessage(result.getMessage());
    }

    private void logAnalysisEndedWithSuccess(Project project) {
        Notifications.Bus.notify(new Notification("sonarrunner", "Analysis success", "Sonar analysis finished successfully on " + project.getName() + "", NotificationType.INFORMATION), project);
    }

    private void logAnalysisStarted(Project project) {
        Notifications.Bus.notify(new Notification("sonarrunner", "Analysis started", "Sonar analysis started on''" + project.getName() + "'", NotificationType.INFORMATION), project);
    }
}
