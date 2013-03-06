package org.sonar.ide.intellij.component;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 2/27/13
 * Time: 8:08 PM
 * To change this template use File | Settings | File Templates.
 */
public interface EventListener {
    void handleEvent(EventKind eventKind);
}
