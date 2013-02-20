package org.sonar.ide.intellij.listener;


/**
 * Generic listener interface notifying that a worker job is done
 * @param <T>
 */
public interface JobDoneListener<T> {
    void jobDone(T result);
}
