package com.allan.openhereplugin.runs.mac;

import com.allan.openhereplugin.runs.abs.IWarpRuns;
import com.intellij.openapi.project.Project;

public class WarpMacRuns implements IWarpRuns {
    @Override
    public boolean checkIfCanRun(Project project) {
        return true;
    }

    private void runGotoNewWindow(String targetDir) {
        try {
            String warpUrl = "warp://action/new_window?path=" + targetDir;
            // macOS 使用 open 命令
            String[] cmd = {"open", warpUrl};
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            //
        }
    }

    private void runGotoNewTab(String targetDir) {
        try {
            String warpUrl = "warp://action/new_tab?path=" + targetDir;
            // macOS 使用 open 命令
            String[] cmd = {"open", warpUrl};
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            //
        }
    }

    @Override
    public void runTab(String gitPath) {
        if (gitPath == null || gitPath.length() <= 2) {
            return;
        }

        runGotoNewTab(gitPath);
    }

//    @Override
//    public void runWindow(String gitPath) {
//        if (gitPath == null || gitPath.length() <= 2) {
//            return;
//        }
//
//        runGotoNewWindow(gitPath);
//    }
}
