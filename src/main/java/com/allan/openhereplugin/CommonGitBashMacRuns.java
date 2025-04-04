package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.IGitBashRuns;

import java.io.IOException;

public class CommonGitBashMacRuns implements IGitBashRuns {

    @Override
    public String system() {
        return Common.SYSTEM_MAC;
    }

    private void runOnlyCd(String targetDirectory) {
        try {
            // 使用 osascript 执行 AppleScript 命令
            ProcessBuilder pb = new ProcessBuilder(
                    "osascript",
                    "-e",
                    "tell application \"Terminal\"",
                    "-e",
                    "activate",
                    "-e",
                    "tell application \"System Events\" to tell process \"Terminal\" to keystroke \"t\" using command down",
                    "-e",
                    "delay 0.8",
                    "-e",
                    "do script \"cd " + targetDirectory + "\" in selected tab of the front window",
                    "-e",
                    "end tell"
            );

            pb.start();
        } catch (IOException e) {
            //
        }
    }

    private void runCdAndCmd(String targetDirectory, String cmd) {
        try {

            // 使用 osascript 执行 AppleScript 命令
            ProcessBuilder pb = new ProcessBuilder(
                    "osascript",
                    "-e",
                    "tell application \"Terminal\"",
                    "-e",
                    "activate",
                    "-e",
                    "tell application \"System Events\" to tell process \"Terminal\" to keystroke \"t\" using command down",
                    "-e",
                    "delay 0.8",
                    "-e",
                    "do script \"cd " + targetDirectory + "\" in selected tab of the front window",
                    "-e",
                    "delay 0.5",
                    "-e",
                    "do script \" " + cmd + "\" in selected tab of the front window",
                    "-e",
                    "end tell"
            );

            pb.start();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void runGitBash(String gitPath) {
        if (gitPath == null || gitPath.length() <= 2) {
            return;
        }
        runOnlyCd(gitPath);
    }

    @Override
    public void runGitDiff(String gitPath, String relativeFile) {
        runCdAndCmd(gitPath, "git diff " + relativeFile);
    }

    @Override
    public void runGitStatus(String gitPath, String relativeDir) {
        String s;
        if (relativeDir.isEmpty()) {
            s = "git status";
        } else {
            s = "git status " + relativeDir;
        }
        runCdAndCmd(gitPath, s);
    }

    @Override
    public void runGitLog(String gitPath, String relativeFile) {
        runCdAndCmd(gitPath, "git log " + relativeFile);
    }
}
