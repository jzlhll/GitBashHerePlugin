package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.PathInfo;
import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class WarpTabAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var warpRuns = Common.warpRunner;
        if (warpRuns == null) return;
        var info = Common.findClosestGitRoot(event);
        if (info != null) {
            if (info instanceof PathInfo) {
                warpRuns.runTab(((PathInfo) info).gitPath);
            } else {
                warpRuns.runTab(info.path);
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(GitOpenHereSettings.getInstance().isSupportWarp());
    }
}
