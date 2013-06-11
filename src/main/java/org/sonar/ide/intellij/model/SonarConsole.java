package org.sonar.ide.intellij.model;


import java.util.ArrayList;
import java.util.Collection;

public class SonarConsole {

    public interface SonarConsoleChangeListener {

        void consoleChanged(String message);

        void clear();
    }

    private static final SonarConsole Instance = new SonarConsole();

    private Collection<SonarConsoleChangeListener> subscribers = new ArrayList<SonarConsoleChangeListener>();

    public static SonarConsole getInstance() {
        return Instance;
    }

    public void addMessage(String message) {
        notifySubscribers(message);

    }

    private void notifySubscribers(String message) {
        for (SonarConsoleChangeListener subscriber : subscribers) {
            subscriber.consoleChanged(message);
        }
    }


    public void subscribe(SonarConsoleChangeListener listener) {
        subscribers.add(listener);
    }

    public void clear() {
        for (SonarConsoleChangeListener subscriber : subscribers) {

            subscriber.clear();
        }
    }
}
