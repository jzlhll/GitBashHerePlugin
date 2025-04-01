package com.allan.openhereplugin.util;

import com.esotericsoftware.minlog.Log;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class Logger {
    private static final StringBuilder logSb = new StringBuilder();

    public static void cacheAdd(String s) {
        d(s);
        logSb.append("allan ").append(s).append("\n");
    }

    public static void cacheClear() {
        logSb.setLength(0);
    }

    public static String cacheFetch() {
        return logSb.toString();
    }

    public static String cacheFetchAndClear() {
        String s = logSb.toString();
        cacheClear();
        return s;
    }

    private static final com.intellij.openapi.diagnostic.Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(Logger.class);
    public static void d(String s) {
        System.out.println("allan " + s);
        Log.warn("allan " + s);
    }

    public static void sendNotification(String message, AnActionEvent event, NotificationType notificationType) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("com.allan.githere_notify")
                .createNotification("GitBashHere", message, notificationType)
                .notify(event.getProject());
    }
}
