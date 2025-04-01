package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.PathInfo;
import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GitBashAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Common.assetGitBashPath(event.getProject(), p->{
            var info = Common.findClosestGitRoot(event);
            if (info != null) {
                if (info instanceof PathInfo) {
                    Common.gitBashRuns.runGitBash(((PathInfo) info).gitPath);
                } else {
                    Common.gitBashRuns.runGitBash(info.path);
                }
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(GitOpenHereSettings.getInstance().isSupportBash());
    }
}
