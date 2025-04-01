package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.PathInfo;
import com.allan.openhereplugin.util.Logger;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.allan.openhereplugin.Common.*;

public class GitBashStatusAction extends AnAction {

    private static final boolean USE_4_DEBUG_LOG = true;

    @Override
    public void actionPerformed(AnActionEvent event) {
        if (USE_4_DEBUG_LOG) {
            var log = Logger.cacheFetchAndClear();
            Logger.sendNotification(log, event, NotificationType.INFORMATION);
        } else {
            Common.assertPath(event.getProject(), project -> {
                var bean = Common.findClosestGitRoot(event);
                if (bean != null) {
                    if (bean instanceof PathInfo) {
                        var info = (PathInfo) bean;
                        runGitStatus(info.gitPath, info.relativePath);
                    } else {
                        runGitBash(bean.path);
                    }
                }
            });
        }
    }
}
