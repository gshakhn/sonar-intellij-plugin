package org.sonar.ide.intellij.common;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.Notifications;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

/**
 * Created with IntelliJ IDEA.
 * User: g_gili
 * Date: 3/5/13
 * Time: 12:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class InMemoryNotificationStack implements Notifications {

    private Stack<Notification> stack = new Stack<Notification>();

    @Override
    public void notify(@NotNull Notification notification) {
        stack.push(notification);
    }

    @Override
    public void register(@NotNull String groupDisplayName, @NotNull NotificationDisplayType defaultDisplayType) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void register(@NotNull String groupDisplayName, @NotNull NotificationDisplayType defaultDisplayType, boolean shouldLog) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Notification getTop() {
        return stack.peek();
    }
}
