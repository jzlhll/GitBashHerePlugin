package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.IWarpRuns;

import java.io.IOException;

public class CommonWarpRuns implements IWarpRuns {
    private static void runCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
        processBuilder.redirectErrorStream(true);                // 合并错误流到输出流
        try {
            processBuilder.start();
            // 处理输入/输出流（避免阻塞！）
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void runTab(String gitPath) {
        if (gitPath == null || gitPath.length() <= 2) {
            return;
        }

        //cmd /c start "" "warp://action/new_tab?path=%CD%"
        String cmd = String.format("start \"\" \"warp://action/new_tab?path=%s\"", gitPath);
        runCommand(cmd);
    }

    @Override
    public void runWindow(String gitPath) {
        if (gitPath == null || gitPath.length() <= 2) {
            return;
        }

        //cmd /c start "" "warp://action/new_tab?path=%CD%"
        String cmd = String.format("start \"\" \"warp://action/new_window?path=%s\"", gitPath);
        runCommand(cmd);
    }
}
