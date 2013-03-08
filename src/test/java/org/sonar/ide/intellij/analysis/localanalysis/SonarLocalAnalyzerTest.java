package org.sonar.ide.intellij.analysis.localanalysis;


import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.IdeaTestCase;
import org.mockito.Mockito;
import org.mockito.internal.matchers.CapturingMatcher;
import org.sonar.ide.intellij.common.InMemoryIntelliJSonarPluginCore;
import org.sonar.ide.intellij.common.InMemoryNotificationStack;
import org.sonar.ide.intellij.component.EventBus;
import org.sonar.ide.intellij.component.EventKind;
import org.sonar.ide.intellij.component.EventListener;
import org.sonar.ide.intellij.component.IntelliJSonarPluginCoreImpl;
import org.sonar.ide.intellij.listener.JobDoneListener;
import org.sonar.ide.intellij.model.SonarConsole;
import org.sonar.ide.intellij.runner.SonarRunnerConsoleMonitor;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class SonarLocalAnalyzerTest extends IdeaTestCase {

    private final InMemoryNotificationStack notificationStack = new InMemoryNotificationStack();
    private SonarLocalAnalyzer analyzer = new SonarLocalAnalyzer();
    private SonarRunnerConsoleMonitor sonarRunnerMonitor;
    private LocalAnalysisWorkerFactory mockFactory;

    private final List<String> messages = new ArrayList<String>();

    private EventListener mockEvent;
    private final LocalAnalysisWorker mockWorker = mock(LocalAnalysisWorker.class);
    private final VirtualFile file = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sonarRunnerMonitor = new SonarRunnerConsoleMonitor();
        mockFactory = mock(LocalAnalysisWorkerFactory.class);
        LocalAnalysisWorkerFactoryImpl.register(mockFactory);
        getProject().getMessageBus().connect().subscribe(Notifications.TOPIC, notificationStack);
        mockEvent = mock(EventListener.class);
        EventBus.subscribe(EventKind.LOCAL_ANALYSIS_FINISHED, mockEvent);
        SonarConsole.getInstance().subscribe(new SonarConsole.SonarConsoleChangeListener() {
            @Override
            public void consoleChanged(String message) {
                messages.add(message);
            }

            @Override
            public void clear() {
                // nothing to do
            }
        });

        IntelliJSonarPluginCoreImpl.register(new InMemoryIntelliJSonarPluginCore());
    }


    public void testRunLocalAnalysisEndsUpError() throws Exception {

        simulateRunAndCompleteWithResult(new RunResult("Error", 1));

        assertTrue(messages.contains("Error"));
        assertEquals(NotificationType.ERROR, notificationStack.getTop().getType());
        assertEquals("Sonar analysis failed check console for more details", notificationStack.getTop().getContent());
    }

    public void testRunLocalAnalysisRunsWithSuccess() throws Exception {
        simulateRunAndCompleteWithResult(new RunResult(new SonarLocalAnalysis(getProject(), null), 0));
        assertTrue(messages.size() == 0);
        assertEquals(NotificationType.INFORMATION, notificationStack.getTop().getType());
        assertEquals("Sonar analysis finished successfully on " + getProject().getName(), notificationStack.getTop().getContent());
    }

    public void testRunLocalAnalysisIsStopped() throws Exception {
        sonarRunnerMonitor.cancelAnalysis();
        simulateRunAndCompleteWithResult(new RunResult("Stopped", 123));

        assertEquals(NotificationType.INFORMATION, notificationStack.getTop().getType());
        assertEquals("Sonar analysis requested to be stopped", notificationStack.getTop().getContent());
    }


    private void simulateRunAndCompleteWithResult(RunResult simulatedResult) {
        CapturingMatcher<JobDoneListener<RunResult>> matcher = simulateRun();

        matcher.getLastValue().jobDone(simulatedResult);
        verify(mockEvent).handleEvent(EventKind.LOCAL_ANALYSIS_FINISHED);
    }

    private CapturingMatcher<JobDoneListener<RunResult>> simulateRun() {
        CapturingMatcher<JobDoneListener<RunResult>> matcher = new CapturingMatcher<JobDoneListener<RunResult>>();
        when(mockFactory.createLocalAnalysisWorker(eq(getProject()), Mockito.eq(file), eq(sonarRunnerMonitor), Mockito.argThat(matcher))).thenReturn(mockWorker);
        analyzer.runLocalAnalysis(getProject(), file, sonarRunnerMonitor);
        return matcher;
    }

    public void testRunWithNotSupportedVersion() throws Exception {
        IntelliJSonarPluginCoreImpl.getInstance().getSonarServer(getProject()).setVersion("1.2");
        simulateRun();
        verify(mockEvent, never()).handleEvent(EventKind.LOCAL_ANALYSIS_STARTED);
        verify(mockEvent, never()).handleEvent(EventKind.LOCAL_ANALYSIS_FINISHED);

        assertEquals(NotificationType.ERROR, notificationStack.getTop().getType());
        assertEquals("Sonar local analysis requires at least 3.4.0 sonar version", notificationStack.getTop().getContent());

    }
}
