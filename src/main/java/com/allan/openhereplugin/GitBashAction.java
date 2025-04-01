package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.PathInfo;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GitBashAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Common.assertPath(event.getProject(), p->{
            var info = Common.findClosestGitRoot(event);
            if (info != null) {
                if (info instanceof PathInfo) {
                    Common.runGitBash(((PathInfo) info).gitPath);
                } else {
                    Common.runGitBash(info.path);
                }
            }
        });
    }

}
