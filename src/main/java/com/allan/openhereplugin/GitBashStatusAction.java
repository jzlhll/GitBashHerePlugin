package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.PathInfo;
import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.allan.openhereplugin.util.Logger;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GitBashStatusAction extends AnAction {

    private static final boolean USE_4_DEBUG_LOG = false;

    @Override
    public void actionPerformed(AnActionEvent event) {
        if (USE_4_DEBUG_LOG) {
            var log = Logger.cacheFetchAndClear();
            Logger.sendNotification(log, event, NotificationType.INFORMATION);
        } else {
            Common.assetGitBashPath(event.getProject(), project -> {
                var bean = Common.findClosestGitRoot(event);
                if (bean != null) {
                    if (bean instanceof PathInfo) {
                        var info = (PathInfo) bean;
                        Common.gitBashRuns.runGitStatus(info.gitPath, info.relativePath);
                    } else {
                        Common.gitBashRuns.runGitBash(bean.path);
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
