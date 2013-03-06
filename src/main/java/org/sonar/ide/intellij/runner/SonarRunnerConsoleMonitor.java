package org.sonar.ide.intellij.runner;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import org.sonar.ide.intellij.model.SonarConsole;

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
public class SonarRunnerConsoleMonitor implements SonarRunnerMonitor {

    private List<String> messages = new ArrayList<String>();

    private int exitCode;

    private boolean requestCancel;

    @Override
    public void startNotified(ProcessEvent event) {
        recordMessage(event);
    }

    private void recordMessage(ProcessEvent event) {
        String text = processText(event.getText());
        if (!StringUtil.isEmpty(text)) {
            messages.add(text);
            SonarConsole.getInstance().addMessage(text);
        }
    }

    private String processText(String text) {
        return text;
    }

    @Override
    public void processTerminated(ProcessEvent event) {
        recordMessage(event);
        exitCode = event.getExitCode();
    }

    @Override
    public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
        recordMessage(event);
    }

    @Override
    public void onTextAvailable(ProcessEvent event, Key outputType) {
        recordMessage(event);
    }

    public void dumpLogs(PrintStream out) {
        for (String message : messages) {
            out.println(message);
        }
    }

    public Collection<String> getNotifications() {
        return Collections.<String>unmodifiableCollection(messages);
    }

    public int getExitCode() {
        return exitCode;
    }


    public void cancelAnalysis() {
        requestCancel = true;
    }

    @Override
    public boolean shouldCancelRequest() {
        return requestCancel;
    }
}
