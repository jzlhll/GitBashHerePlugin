package com.allan.openhereplugin.runs.abs;

import com.allan.openhereplugin.Common;
import com.allan.openhereplugin.runs.mac.GitBashMacRuns;
import com.allan.openhereplugin.runs.windows.GitBashWindowsRuns;

import javax.annotation.Nullable;

public interface IGitBashRuns extends IRuns{
    void runGitBash(String gitPath);
    void runGitDiff(String gitPath, String relativeFile);
    void runGitStatus(String gitPath, String relativeDir);
    void runGitLog(String gitPath, String relativeFile);

    @Nullable
    static IGitBashRuns create() {
        var sys = Common.supportSystem();
        if (Common.SYSTEM_MAC.equals(sys)) {
            return new GitBashMacRuns();
        }
        if (Common.SYSTEM_WINDOWS.equals(sys)) {
            return new GitBashWindowsRuns();
        }
        return null;
    }
}