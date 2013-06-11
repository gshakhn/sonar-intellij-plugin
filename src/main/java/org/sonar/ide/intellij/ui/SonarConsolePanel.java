package org.sonar.ide.intellij.ui;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.impl.ContentImpl;
import org.sonar.ide.intellij.actions.RunLocalAnalysisAction;
import org.sonar.ide.intellij.component.EventBus;
import org.sonar.ide.intellij.component.EventKind;
import org.sonar.ide.intellij.component.EventListener;
import org.sonar.ide.intellij.model.SonarConsole;

import java.awt.*;

public class SonarConsolePanel implements SonarConsole.SonarConsoleChangeListener {

    private final ConsoleView console;
    private final ActionToolbar toolbar = createToolbar();

    private SimpleToolWindowPanel mainPanel;

    public SonarConsolePanel(Project project) {

        mainPanel = new SimpleToolWindowPanel(false, true);

        console = new ConsoleViewImpl(project, false);
        final Content adbLogsContent = new ContentImpl(console.getComponent(), "Sonar runner console", false);
        mainPanel.setContent(adbLogsContent.getComponent());

        mainPanel.setToolbar(toolbar.getComponent());
        SonarConsole.getInstance().subscribe(this);

        EventBus.subscribe(EventKind.LOCAL_ANALYSIS_FINISHED, new EventListener() {
            @Override
            public void handleEvent(EventKind eventKind) {
                toolbar.updateActionsImmediately();
            }
        });
    }

    private ActionToolbar createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new StopAnalysisAction());

        return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, false);
    }

    @Override
    public void consoleChanged(String message) {
        console.print(message, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    @Override
    public void clear() {
        console.clear();
    }

    public Component getMainPanel() {
        return mainPanel;
    }

    private class StopAnalysisAction extends DumbAwareAction {

        private StopAnalysisAction() {
            super("Stop", "Stop running analysis", AllIcons.Actions.Suspend);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            EventBus.notifyEvent(EventKind.CANCEL_LOCAL_ANALYSIS);
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(RunLocalAnalysisAction.getSonarLocalAnalyzer().isLocalAnalysisRunning());
            super.update(e);
        }
    }
}
