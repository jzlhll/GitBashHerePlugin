package com.allan.openhereplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.allan.openhereplugin.Common.*;

public class GitBashDiffAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Common.assertPath(event.getProject(), project -> {
            var bean = Common.findClosestGitRoot(event);
            if (bean != null) {
                if (bean instanceof PathInfo) {
                    var info = (PathInfo) bean;
                    runGitDiff(info.gitPath, info.relativePath);
                } else {
                    runGitBash(bean.path);
                }
            }
        });
    }
}
