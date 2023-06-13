package com.allan.openhereplugin;

import com.allan.openhereplugin.beans.PathsBean;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.allan.openhereplugin.Common.*;

public class GitBashExtraAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Common.assertPath(event.getProject(), project -> {
            var bean = (PathsBean)Common.pathToDirectoryForProject(event, false);
            runGitStatus(bean.deepGitDirectoryPath, bean.relativeToDeepGitPath);
        });
    }
}
