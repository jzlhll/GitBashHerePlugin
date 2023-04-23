package com.allan.openhereplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.allan.openhereplugin.Common.*;

public class GitBashStatusAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Common.assertPath(event.getProject(), project -> {
            var bean = (PathsBean)Common.pathToDirectoryForProject(event, false);
            runGitStatus(bean.deepGitDirectoryPath, bean.relativeToDeepGitPath);
        });
    }
}
