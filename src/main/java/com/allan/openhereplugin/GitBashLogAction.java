package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.PathInfo;
import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import org.jetbrains.annotations.NotNull;

public class GitBashLogAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Common.assetGitBashPath(event.getProject(), project -> {
            var bean = Common.findClosestGitRoot(event);
            if (bean != null) {
                var thisFileStr = event.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE).toString().replace("file://", "");
                if (bean instanceof PathInfo) {
                    var info = (PathInfo) bean;
                    //runGitLog(info.gitPath, info.relativePath);
                } else {
                    Common.gitBashRuns.runGitBash(bean.path);
                }
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        boolean isHide = GitOpenHereSettings.getInstance().getState().isGitLogChecked;
        if (!GitOpenHereSettings.getInstance().isSupportBash()) {
            isHide = true;
        }
        e.getPresentation().setVisible(!isHide);
    }
}
