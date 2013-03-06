package org.sonar.ide.intellij.runner;

import com.intellij.execution.process.ProcessListener;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 3/1/13
 * Time: 9:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SonarRunnerMonitor extends ProcessListener {

    boolean shouldCancelRequest();
}
