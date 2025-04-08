package com.allan.openhereplugin.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@State(name = "GitOpenHereSettings", storages = @Storage("gitbashopenhere01.xml"))
public class GitOpenHereSettings implements PersistentStateComponent<GitOpenHereSettings.State> {
    public static final int GIT_TOOL_TYPE_BASH_AND_WARP = 2;
    public static final int GIT_TOOL_TYPE_BASH = 0;
    public static final int GIT_TOOL_TYPE_WARP = 1;

    public static final int WINDOW_CMD_TYPE_CMD = 1;
    public static final int WINDOW_CMD_TYPE_POWER_CMD = 2;
    public static final int WINDOW_CMD_TYPE_NO = 0;

    private State state = new State();

    public boolean isSupportBash() {
        return state.gitToolType == GIT_TOOL_TYPE_BASH || state.gitToolType == GIT_TOOL_TYPE_BASH_AND_WARP;
    }

    public boolean isSupportWarp() {
        return state.gitToolType == GIT_TOOL_TYPE_WARP || state.gitToolType == GIT_TOOL_TYPE_BASH_AND_WARP;
    }

    public static GitOpenHereSettings getInstance() {
        return ApplicationManager.getApplication().getService(GitOpenHereSettings.class);
    }

    // 配置数据结构
    public static class State {
        public String gitBashCustomPath = "";

       // public boolean isWarpTabChecked = false;

        public boolean isGitStatusChecked = false;
        public boolean isGitDiffChecked = false;
        public boolean isGitLogChecked = false;
        public boolean isCopyNameChecked = false;
        public boolean isCopyNameNoExChecked = false;

        public int windowCmdType = WINDOW_CMD_TYPE_CMD;

        public int gitToolType = GIT_TOOL_TYPE_BASH;
    }

    @Override
    public @NotNull State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }
}