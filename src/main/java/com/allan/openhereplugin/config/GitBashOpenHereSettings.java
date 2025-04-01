package com.allan.openhereplugin.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@State(name = "GitBashOpenHereSettings", storages = @Storage("GitBashOpenHereSettings.xml"))
public class GitBashOpenHereSettings implements PersistentStateComponent<GitBashOpenHereSettings.State> {
    private State state = new State();

    public static GitBashOpenHereSettings getInstance() {
        return ApplicationManager.getApplication().getService(GitBashOpenHereSettings.class);
    }

    // 配置数据结构
    public static class State {
        public String gitToolExePath = "";
        public boolean isGitStatusChecked = false;
        public boolean isGitDiffChecked = false;
        public boolean isCustomGitToolChecked = false;
        public boolean isGitLogChecked = false;
        public boolean isCopyNameChecked = false;
        public boolean isCopyNameNoExChecked = false;
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