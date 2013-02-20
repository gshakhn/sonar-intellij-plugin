package org.sonar.ide.intellij.runner;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.util.Key;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 2/13/13
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class SonarRunnerMonitor implements ProcessListener {

    private List<String> messages = new ArrayList<String>();

    private int exitCode;

    @Override
    public void startNotified(ProcessEvent event) {
        messages.add(event.getText());
    }

    @Override
    public void processTerminated(ProcessEvent event) {
        messages.add(event.getText());
        exitCode = event.getExitCode();
    }

    @Override
    public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
        messages.add(event.getText());
    }

    @Override
    public void onTextAvailable(ProcessEvent event, Key outputType) {
        messages.add(event.getText());
    }

    public void dumpLogs(PrintStream out) {
        for (String message : messages) {
            out.println(message);
        }
    }

    public Collection<String> getNotificatios() {
        return Collections.<String>unmodifiableCollection(messages);
    }
}
