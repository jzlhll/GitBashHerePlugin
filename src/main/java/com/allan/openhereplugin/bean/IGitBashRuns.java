package com.allan.openhereplugin.bean;

import com.allan.openhereplugin.Common;
import com.allan.openhereplugin.CommonGitBashMacRuns;
import com.allan.openhereplugin.CommonGitBashRuns;

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
            return new CommonGitBashMacRuns();
        }
        if (Common.SYSTEM_WINDOWS.equals(sys)) {
            return new CommonGitBashRuns();
        }
        return null;
    }
}