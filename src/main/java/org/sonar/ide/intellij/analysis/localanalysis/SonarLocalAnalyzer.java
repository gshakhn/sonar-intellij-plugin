package org.sonar.ide.intellij.analysis.localanalysis;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.component.EventBus;
import org.sonar.ide.intellij.component.EventKind;
import org.sonar.ide.intellij.component.IntelliJSonarPluginCoreImpl;
import org.sonar.ide.intellij.component.SonarProjectComponent;
import org.sonar.ide.intellij.listener.JobDoneListener;
import org.sonar.ide.intellij.model.SonarConsole;
import org.sonar.ide.intellij.model.Version;
import org.sonar.ide.intellij.runner.SonarRunnerConsoleMonitor;
import org.sonar.wsclient.services.Server;

/**
 * Main entry point for doing local analysis
 */
public class SonarLocalAnalyzer {

    private final static Version MINIMUM_SUPPORTED_VERSION = new Version(3, 4, 0);

    private LocalAnalysisWorker localAnalysisWorker;

    public void runLocalAnalysis(final Project project, final VirtualFile file, final SonarRunnerConsoleMonitor sonarRunnerMonitor) {

        checkAnalysisIsRunning();

        synchronized (this) {
            checkAnalysisIsRunning();

            if (isSonarCompatible(project)) {
                localAnalysisWorker = LocalAnalysisWorkerFactoryImpl.getInstance().createLocalAnalysisWorker(project, file, sonarRunnerMonitor, new JobDoneListener<RunResult>() {
                    @Override
                    public void jobDone(RunResult result) {

                        EventBus.notifyEvent(EventKind.LOCAL_ANALYSIS_FINISHED);

                        handleAnalysisResult(result, project, sonarRunnerMonitor);

                        localAnalysisWorker = null;
                    }
                });

                localAnalysisWorker.execute();
                logAnalysisStarted(project);
            } else {
                reportSonarNotCompatible(project);
            }


        }
    }

    private void handleAnalysisResult(RunResult result, Project project, SonarRunnerConsoleMonitor sonarRunnerMonitor) {
        SonarProjectComponent sonarProjectComponent = project.getComponent(SonarProjectComponent.class);

        if (result.isSuccess()) {
            logAnalysisEndedWithSuccess(project);
            sonarProjectComponent.switchToLocalAnalysis(result.getLocalAnalysisAnalysis());
        } else if (sonarRunnerMonitor.shouldCancelRequest()) {
            logAnalysisStopped(project);
        } else {
            logAnalysisEndedInError(result, project);
        }
    }

    private void reportSonarNotCompatible(Project project) {
        Notifications.Bus.notify(new Notification("sonarrunner", "Incompatible sonar version", String.format("Sonar local analysis requires at least %s sonar version", MINIMUM_SUPPORTED_VERSION.getVersionAsString()), NotificationType.ERROR), project);
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
        EventBus.notifyEvent(EventKind.LOCAL_ANALYSIS_STARTED);
    }

    public boolean isSonarCompatible(Project project) {

        String version = getVersion(project);
        return MINIMUM_SUPPORTED_VERSION.compareTo(Version.parse(version)) <= 0;
    }

    private String getVersion(Project project) {
        Server sonarServer = IntelliJSonarPluginCoreImpl.getInstance().getSonarServer(project);
        String version = sonarServer.getVersion();
        int i = version.indexOf('-');
        if (i != -1) {
            version = version.substring(0, i);
        }
        return version;
    }
}
