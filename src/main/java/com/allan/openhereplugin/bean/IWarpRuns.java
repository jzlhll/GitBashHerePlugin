package com.allan.openhereplugin.bean;

import com.allan.openhereplugin.*;

import javax.annotation.Nullable;

public interface IWarpRuns extends IRuns{
    void runTab(String gitPath);
    void runWindow(String gitPath);

    @Nullable
    static IWarpRuns create() {
        var sys = Common.supportSystem();
        if (Common.SYSTEM_MAC.equals(sys)) {
            return new CommonWarpMacRuns();
        }
        if (Common.SYSTEM_WINDOWS.equals(sys)) {
            return new CommonWarpRuns();
        }
        return null;
    }
}