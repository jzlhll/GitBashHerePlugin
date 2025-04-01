package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.PathInfo;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;

import static com.allan.openhereplugin.Common.runGitBash;

public class GitBashLogAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Common.assertPath(event.getProject(), project -> {
            var bean = Common.findClosestGitRoot(event);
            if (bean != null) {
                var thisFileStr = event.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE).toString().replace("file://", "");
                if (bean instanceof PathInfo) {
                    var info = (PathInfo) bean;
                    //runGitLog(info.gitPath, info.relativePath);
                } else {
                    runGitBash(bean.path);
                }
            }
        });
    }
}
