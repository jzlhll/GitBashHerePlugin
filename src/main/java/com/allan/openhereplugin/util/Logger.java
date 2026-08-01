package com.allan.openhereplugin.util;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class Logger {
    private static final boolean DEBUG_ENABLED = false;

    private static final com.intellij.openapi.diagnostic.Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(Logger.class);
    public static void d(String s) {
        if (DEBUG_ENABLED) {
            LOG.warn("allan " + s);
        }
    }

    public static void sendNotification(String message, AnActionEvent event, NotificationType notificationType) {
        sendNotification(message, event.getProject(), notificationType);
    }

    public static void sendNotification(String message, com.intellij.openapi.project.Project project, NotificationType notificationType) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("com.allan.githere_notify")
                .createNotification("GitBashHere", message, notificationType)
                .notify(project);
    }
}
