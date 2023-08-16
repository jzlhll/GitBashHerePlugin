package com.allan.openhereplugin;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.io.File;

public class GitBashCopyNameAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            var thisFile = event.getDataContext().getData("virtualFile").toString().replace("file://", "");
            var name = new File(thisFile).getName();
            CopyPasteManager.getInstance().setContents(new StringSelection(name));

            NotificationUtil.sendNotification("copied success! " + name, event, NotificationType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}