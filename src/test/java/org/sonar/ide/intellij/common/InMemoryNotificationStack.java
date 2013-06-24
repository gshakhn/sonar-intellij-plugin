package org.sonar.ide.intellij.common;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.Notifications;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;


public class InMemoryNotificationStack implements Notifications {

    private Stack<Notification> stack = new Stack<Notification>();

    @Override
    public void notify(@NotNull Notification notification) {
        stack.push(notification);
    }

    @Override
    public void register(@NotNull String groupDisplayName, @NotNull NotificationDisplayType defaultDisplayType) {
    }

    @Override
    public void register(@NotNull String groupDisplayName, @NotNull NotificationDisplayType defaultDisplayType, boolean shouldLog) {
    }

    public Notification getTop() {
        return stack.peek();
    }
}
