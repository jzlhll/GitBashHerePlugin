package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.PathInfo;
import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.allan.openhereplugin.util.Logger;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;

public class GitBashStatusAction extends AnAction {

    private static final boolean USE_4_DEBUG_LOG = false;

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var gitBashRuns = Common.gitBashRunner;
        if (gitBashRuns == null) return;

        if (USE_4_DEBUG_LOG) {
            var log = Logger.cacheFetchAndClear();
            CopyPasteManager.getInstance().setContents(new StringSelection(log));
            Logger.sendNotification("Copy and clear logs!\n" + log, event, NotificationType.INFORMATION);
        } else {
            gitBashRuns.checkIfCanRun(event.getProject(), ()->{
                var bean = Common.findClosestGitRoot(event);
                if (bean != null) {
                    if (bean instanceof PathInfo) {
                        var info = (PathInfo) bean;
                        gitBashRuns.runGitStatus(info.gitPath, info.relativePath);
                    } else {
                        gitBashRuns.runGitBash(bean.path);
                    }
                }
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        boolean isHide = GitOpenHereSettings.getInstance().getState().isGitStatusChecked;
        if (!GitOpenHereSettings.getInstance().isSupportBash()) {
            isHide = true;
        }
        e.getPresentation().setVisible(!isHide);
    }
}
