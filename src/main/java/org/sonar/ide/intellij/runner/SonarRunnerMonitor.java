package org.sonar.ide.intellij.runner;

import com.intellij.execution.process.ProcessListener;


public interface SonarRunnerMonitor extends ProcessListener {

    boolean shouldCancelRequest();
}
