package com.allan.openhereplugin;

import com.allan.openhereplugin.util.Logger;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ShowProjectPathAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        String projectPath = project == null ? null : project.getBasePath();
        if (projectPath == null) {
            return;
        }

        Logger.sendNotification(projectPath, event, NotificationType.INFORMATION);
    }
}
