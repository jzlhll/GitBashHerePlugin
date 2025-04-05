package com.allan.openhereplugin;

import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.allan.openhereplugin.util.Logger;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.io.File;

public class GitBashCopyNameAction extends AnAction {

    public String changeName(String name) {
        return name;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            var vf = event.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE);
            if (vf == null) {
                return;
            }
            var thisFile = vf.toString().replace("file://", "");

            var name = new File(thisFile).getName();
            name = changeName(name);
            CopyPasteManager.getInstance().setContents(new StringSelection(name));

            Logger.sendNotification("copied success! " + name, event, NotificationType.INFORMATION);
        } catch (Exception e) {
            //e.printStackTrace();
        }

    }

    @Override
    public final void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(isNeedShow());
    }

    protected boolean isNeedShow() {
        return !GitOpenHereSettings.getInstance().getState().isCopyNameChecked;
    }
}