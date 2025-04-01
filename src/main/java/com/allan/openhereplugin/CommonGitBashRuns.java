package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.IGitBashRuns;
import com.allan.openhereplugin.bean.Pair;
import com.allan.openhereplugin.config.GitOpenHereSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.allan.openhereplugin.Common.*;
import static com.allan.openhereplugin.util.Util.kot;

public class CommonGitBashRuns implements IGitBashRuns {

    @Override
    public Pair findPath() {
        boolean isWin = isWindows();
        if (!isWin) {
            return new Pair("notWin", "");
        }

        if (GitOpenHereSettings.getInstance().getState().gitToolType == 0) {
            var customPath = GitOpenHereSettings.getInstance().getState().gitBashCustomPath;
            if (customPath != null && !customPath.isEmpty()) {
                if (new File(customPath).isFile()) {
                    return new Pair("ok", customPath);
                }
                return new Pair("customError", "");
            }
        }

        if (gitBashPath != null) {
            boolean isNoPath = gitBashPath.equals(NOT_FOUND_GITBASH_PATH);
            if (isNoPath) {
                return new Pair("no", "");
            } else {
                return new Pair("ok", gitBashPath);
            }
        }

        var profile = System.getenv("ProgramFiles");
        if (!profile.endsWith("\\")) {
            profile = profile + "\\";
        }
        var systemGitPath = profile + "Git\\git-bash.exe";
        String[] listOfPresetsGitBash = {
                systemGitPath,
                "C:\\Program Files\\Git\\git-bash.exe",
                "C:\\Program Files (x86)\\Git\\git-bash.exe",
                "D:\\Program Files\\Git\\git-bash.exe",
                "D:\\Program Files (x86)\\Git\\git-bash.exe",
                System.getenv("LOCALAPPDATA") + "\\Programs\\Git\\git-bash.exe"
        };

        for (var path : listOfPresetsGitBash) {
            if (Files.exists(Path.of(path))) {
                gitBashPath = path;
                return new Pair("ok", gitBashPath);
            }
        }

        gitBashPath = NOT_FOUND_GITBASH_PATH;
        return new Pair("no", "");
    }

    /**
     * 必须在assetPath之后调用
     */
    private String gitToolPathAfterAsset() {
        if (GitOpenHereSettings.getInstance().getState().gitToolType == 0) {
            var path = GitOpenHereSettings.getInstance().getState().gitBashCustomPath;
            if(!path.isEmpty()) {
                return path;
            }
        }
        return gitBashPath;
    }

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
    public void runGitBash(String gitPath) {
        if (gitPath == null || gitPath.length() <= 2) {
            return;
        }

        var disk = gitPath.substring(0, 2);
        String cmd = String.format("%s && cd %s && %s", disk, gitPath,
                kot(gitToolPathAfterAsset()));
        runCommand(cmd);
    }

    @Override
    public void runGitDiff(String gitPath, String relativeFile) {
        runGitBashCmds(gitPath, new String[]{"git diff " + relativeFile});
    }

    @Override
    public void runGitStatus(String gitPath, String relativeDir) {
        String s;
        if (relativeDir.isEmpty()) {
            s = "git status";
        } else {
            s = "git status " + relativeDir;
        }
        runGitBashCmds(gitPath, new String[]{s});
    }

    @Override
    public void runGitLog(String gitPath, String relativeFile) {
        runGitBashCmds(gitPath, new String[]{"git log " + relativeFile});
    }

    private void runGitBashCmds(String gitPath, String[] extraCmds) {
        if (gitPath == null || gitPath.isEmpty()) {
            return;
        }
        var disk = gitPath.substring(0, 2);

        StringBuilder gitCmds = new StringBuilder();

        for (String c : extraCmds) {
            gitCmds.append(c).append(" && ");
        }
        gitCmds.append("/usr/bin/bash --login -i");

        //start "" "%ProgramFiles%\Git\git-bash.exe" -c "echo 1 && echo 2 && /usr/bin/bash --login -i"
        String cmd = String.format("%s && cd %s && start \"\" %s -c %s",
                disk,
                gitPath,
                kot(gitToolPathAfterAsset()),
                kot(gitCmds.toString()));
        runCommand(cmd);
    }
}
