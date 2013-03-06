package org.sonar.ide.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.analysis.localanalysis.SonarLocalAnalyzer;
import org.sonar.ide.intellij.component.EventBus;
import org.sonar.ide.intellij.component.EventKind;
import org.sonar.ide.intellij.component.EventListener;
import org.sonar.ide.intellij.model.SonarConsole;
import org.sonar.ide.intellij.runner.SonarRunnerConsoleMonitor;

public class RunLocalAnalysisAction extends DumbAwareAction {

    private static final SonarLocalAnalyzer sonarLocalAnalyzer = new SonarLocalAnalyzer();


    public static SonarLocalAnalyzer getSonarLocalAnalyzer() {
        return sonarLocalAnalyzer;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        final Project project = anActionEvent.getData(PlatformDataKeys.PROJECT);
        if (project == null || project.isDefault()) {
            return;
        }

        final VirtualFile file = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file == null) {
            return;
        }

        SonarConsole.getInstance().clear();

        final SonarRunnerConsoleMonitor sonarRunnerMonitor = new SonarRunnerConsoleMonitor();
        EventBus.subscribe(EventKind.CANCEL_LOCAL_ANALYSIS, new EventListener() {
            @Override
            public void handleEvent(EventKind eventKind) {
                if (sonarLocalAnalyzer.isLocalAnalysisRunning()) {
                    sonarRunnerMonitor.cancelAnalysis();
                }
            }
        });
        sonarLocalAnalyzer.runLocalAnalysis(project, file, sonarRunnerMonitor);

    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(!sonarLocalAnalyzer.isLocalAnalysisRunning());
    }

}
