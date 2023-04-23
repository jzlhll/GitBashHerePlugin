package com.allan.openhereplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GitBashAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Common.assertPath(event.getProject(), p->{
            PathsBean bean = (PathsBean) Common.pathToDirectoryForProject(event, false);
            Common.runGitBash(bean.deepGitDirectoryPath);
        });
    }
}
