package com.allan.openhereplugin;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class NotificationUtil {
    static void sendNotification(String message, AnActionEvent event, NotificationType notificationType) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("com.allan.githere_notify")
                .createNotification(message, notificationType)
                .notify(event.getProject());
    }
}