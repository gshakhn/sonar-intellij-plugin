package org.sonar.ide.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sonar.ide.intellij.component.SonarProjectComponent;
import org.sonar.ide.intellij.listener.JobDoneListener;
import org.sonar.ide.intellij.runner.SonarRunnerMonitor;
import org.sonar.ide.intellij.utils.SonarLocalAnalysisAnalysis;
import org.sonar.ide.intellij.worker.LocalAnalysisWorker;

public class RunLocalAnalysisAction extends DumbAwareAction {
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

        LocalAnalysisWorker localAnalysisWorker = new LocalAnalysisWorker(project, file, new SonarRunnerMonitor(), new JobDoneListener<SonarLocalAnalysisAnalysis>() {
            @Override
            public void jobDone(SonarLocalAnalysisAnalysis result) {
                SonarProjectComponent sonarProjectComponent = project.getComponent(SonarProjectComponent.class);
                sonarProjectComponent.switchToLocalAnalysis(result);
            }
        });
        localAnalysisWorker.execute();
    }
}
