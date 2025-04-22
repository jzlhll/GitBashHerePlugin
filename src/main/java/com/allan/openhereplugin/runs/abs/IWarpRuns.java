package com.allan.openhereplugin.runs.abs;

import com.allan.openhereplugin.*;
import com.allan.openhereplugin.runs.mac.WarpMacRuns;
import com.allan.openhereplugin.runs.windows.WarpWindowsRuns;

import javax.annotation.Nullable;

public interface IWarpRuns extends IRuns{
    void runTab(String gitPath);
    //void runWindow(String gitPath);

    @Nullable
    static IWarpRuns create() {
        var sys = Common.supportSystem();
        if (Common.SYSTEM_MAC.equals(sys)) {
            return new WarpMacRuns();
        }
        if (Common.SYSTEM_WINDOWS.equals(sys)) {
            return new WarpWindowsRuns();
        }
        return null;
    }
}