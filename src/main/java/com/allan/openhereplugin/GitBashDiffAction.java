package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.PathInfo;
import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.allan.openhereplugin.runs.abs.IGitBashRuns;
import org.jetbrains.annotations.NotNull;

public class GitBashDiffAction extends GitBashCommandAction {

    @Override
    protected void runGitCommand(@NotNull IGitBashRuns gitBashRuns, @NotNull PathInfo pathInfo) {
        gitBashRuns.runGitDiff(pathInfo.gitPath, pathInfo.relativePath);
    }

    @Override
    protected boolean isHidden(@NotNull GitOpenHereSettings.State state) {
        return state.isGitDiffChecked;
    }
}
