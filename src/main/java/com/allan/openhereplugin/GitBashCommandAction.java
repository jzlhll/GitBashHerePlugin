package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.NoGitPathInfo;
import com.allan.openhereplugin.bean.PathInfo;
import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.allan.openhereplugin.runs.abs.IGitBashRuns;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/** 在最近的 Git 根目录执行命令的菜单动作基类。 */
public abstract class GitBashCommandAction extends AnAction {

    @Override
    public final void actionPerformed(@NotNull AnActionEvent event) {
        IGitBashRuns gitBashRuns = Common.gitBashRunner;
        if (gitBashRuns == null) {
            return;
        }
        gitBashRuns.checkIfCanRun(event.getProject(), () -> runCommand(event, gitBashRuns));
    }

    private void runCommand(@NotNull AnActionEvent event, @NotNull IGitBashRuns gitBashRuns) {
        NoGitPathInfo pathInfo = Common.findClosestGitRoot(event);
        if (pathInfo instanceof PathInfo) {
            runGitCommand(gitBashRuns, (PathInfo) pathInfo);
        } else if (pathInfo != null) {
            gitBashRuns.runGitBash(pathInfo.path);
        }
    }

    @Override
    public final void update(@NotNull AnActionEvent event) {
        super.update(event);
        GitOpenHereSettings settings = GitOpenHereSettings.getInstance();
        event.getPresentation().setVisible(settings.isSupportBash() && !isHidden(settings.getState()));
    }

    protected abstract void runGitCommand(@NotNull IGitBashRuns gitBashRuns, @NotNull PathInfo pathInfo);

    protected abstract boolean isHidden(@NotNull GitOpenHereSettings.State state);
}
