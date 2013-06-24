package org.sonar.ide.intellij.component;


public interface EventListener {
    void handleEvent(EventKind eventKind);
}
