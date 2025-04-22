package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.PathInfo;
import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GitBashDiffAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var gitBashRuns = Common.gitBashRunner;
        if (gitBashRuns == null) return;

        boolean canRun = gitBashRuns.checkIfCanRun(event.getProject());
        if (canRun) {
            var bean = Common.findClosestGitRoot(event);
            if (bean != null) {
                if (bean instanceof PathInfo) {
                    var info = (PathInfo) bean;
                    gitBashRuns.runGitDiff(info.gitPath, info.relativePath);
                } else {
                    gitBashRuns.runGitBash(bean.path);
                }
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        boolean isHide = GitOpenHereSettings.getInstance().getState().isGitDiffChecked;
        if (!GitOpenHereSettings.getInstance().isSupportBash()) {
            isHide = true;
        }
        e.getPresentation().setVisible(!isHide);
    }
}
