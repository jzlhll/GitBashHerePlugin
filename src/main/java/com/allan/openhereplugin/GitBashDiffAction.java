package com.allan.openhereplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.allan.openhereplugin.Common.*;

public class GitBashDiffAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Common.assertPath(event.getProject(), project -> {
            var bean = (DiffsBean) Common.pathToDirectoryForProject(event, true);
            runGitDiff(bean.deepGitDirectoryPath, bean.relativeToDeepGitPathFile);
        });
    }
}
