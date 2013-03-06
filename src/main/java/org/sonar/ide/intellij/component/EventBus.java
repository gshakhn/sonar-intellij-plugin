package org.sonar.ide.intellij.component;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Experimental event bus for loosely connecting different component together
 */
public class EventBus {

    public static Map<EventKind, Set<EventListener>> Listeners = new ConcurrentHashMap<EventKind, Set<EventListener>>();

    static {
        for (EventKind eventKind : EventKind.values()) {
            Listeners.put(eventKind, new HashSet<EventListener>());
        }

    }

    public static void subscribe(EventKind eventKind, EventListener listener) {
        Listeners.get(eventKind).add(listener);
    }

    public static void notifyEvent(EventKind eventKind) {
        for (EventListener eventListener : Listeners.get(eventKind)) {
            eventListener.handleEvent(eventKind);
        }

    }
}
