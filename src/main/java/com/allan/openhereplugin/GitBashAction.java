package com.allan.openhereplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GitBashAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Common.assertPath(event.getProject(), p->{
            PathInfo info = Common.findClosestGitRoot(event);
            Common.runGitBash(info.gitPath);
        });
    }

}
