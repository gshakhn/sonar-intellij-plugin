package org.sonar.ide.intellij.ui;


import junit.framework.Assert;
import org.junit.Test;
import org.sonar.ide.intellij.component.EventBus;
import org.sonar.ide.intellij.component.EventKind;
import org.sonar.ide.intellij.component.EventListener;

public class EventBusTest {

    @Test
    public void testEventNotified() throws Exception {
        EventBus.subscribe(EventKind.LOCAL_ANALYSIS_ACTIVATED, new EventListener() {
            @Override
            public void handleEvent(EventKind eventKind) {
                Assert.assertEquals(eventKind, EventKind.LOCAL_ANALYSIS_ACTIVATED);
            }
        });

        EventBus.notifyEvent(EventKind.LOCAL_ANALYSIS_ACTIVATED);
    }


    @Test
    public void testNotInterestedEvent() throws Exception {
        EventBus.subscribe(EventKind.LOCAL_ANALYSIS_ACTIVATED, new EventListener() {
            @Override
            public void handleEvent(EventKind eventKind) {
                Assert.assertEquals(eventKind, EventKind.LOCAL_ANALYSIS_ACTIVATED);
            }
        });

        EventBus.notifyEvent(EventKind.LOCAL_ANALYSIS_FINISHED);
    }

    @Test
    public void testWithTwoListeners() throws Exception {
        EventBus.subscribe(EventKind.LOCAL_ANALYSIS_ACTIVATED, new EventListener() {
            @Override
            public void handleEvent(EventKind eventKind) {
                Assert.assertEquals(eventKind, EventKind.LOCAL_ANALYSIS_ACTIVATED);
            }
        });
        EventBus.subscribe(EventKind.LOCAL_ANALYSIS_ACTIVATED, new EventListener() {
            @Override
            public void handleEvent(EventKind eventKind) {
                Assert.assertEquals(eventKind, EventKind.LOCAL_ANALYSIS_ACTIVATED);
            }
        });
        EventBus.subscribe(EventKind.LOCAL_ANALYSIS_ACTIVATED, new EventListener() {
            @Override
            public void handleEvent(EventKind eventKind) {
                Assert.assertEquals(eventKind, EventKind.LOCAL_ANALYSIS_ACTIVATED);
            }
        });

        EventBus.notifyEvent(EventKind.LOCAL_ANALYSIS_ACTIVATED);
        EventBus.notifyEvent(EventKind.LOCAL_ANALYSIS_FINISHED);
    }
}
