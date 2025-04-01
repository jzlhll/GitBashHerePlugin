package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.IFindGitBashPathCallback;
import com.allan.openhereplugin.bean.NoGitPathInfo;
import com.allan.openhereplugin.bean.PathInfo;
import com.allan.openhereplugin.config.GitBashOpenHereConfigurable;
import com.allan.openhereplugin.config.GitBashOpenHereSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Common {
    public static String gitBashPath;

    public static String findGitBashPath() {
        boolean isWin = isWindows();
        if (!isWin) {
            return "notWin";
        }

        if (gitBashPath != null) {
            boolean isNoPath = gitBashPath.equals(NOT_FOUND_GITBASH_PATH);
            if (isNoPath) {
                return "no";
            } else {
                return "ok";
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
        };
        for (var path : listOfPresetsGitBash) {
            if (Files.exists(Path.of(path))) {
                gitBashPath = path;
                return "ok";
            }
        }

        gitBashPath = NOT_FOUND_GITBASH_PATH;
        return "no";
    }

    private static final String NOT_FOUND_GITBASH_PATH = "-no-found-git-bash";

    static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows系统
            return true;
        } else if (os.contains("mac")) {
            // macOS系统
        } else {
            // 其他系统
        }
        return false;
    }

    public static String findGitBashPathWrapCustom() {
        var self = findGitBashPath();

        if (GitBashOpenHereSettings.getInstance().getState().isCustomGitToolChecked) {
            var customPath = GitBashOpenHereSettings.getInstance().getState().gitToolExePath;
            if (customPath != null && !customPath.isEmpty()) {
                if (new File(customPath).isFile()) {
                    return "ok";
                }
                return "customError";
            }
        }

        return self;
    }

    /**
     * 必须在assetPath之后调用
     */
    private static String gitToolPathAfterAsset() {
        if (GitBashOpenHereSettings.getInstance().getState().isCustomGitToolChecked) {
            return GitBashOpenHereSettings.getInstance().getState().gitToolExePath;
        }
        return gitBashPath;
    }

    public static void runCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
        processBuilder.redirectErrorStream(true);                // 合并错误流到输出流
        try {
            processBuilder.start();
            // 处理输入/输出流（避免阻塞！）
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void assertPath(Project project, IFindGitBashPathCallback callback) {
        if (project == null) {
            return;
        }

        switch (findGitBashPathWrapCustom()) {
            case "notWin": {
                int txt = Messages.showOkCancelDialog("message", "Not support linux/mac.", Messages.getOkButton(), Messages.getCancelButton(), Messages.getInformationIcon());
                Messages.showMessageDialog(project, String.valueOf(txt), "OK", Messages.getInformationIcon());
                return;
            }
            case "no": {
                int txt = Messages.showOkCancelDialog("message", "Cannot found git-bash.exe path, please custom your path in Settings(or Other Settings).", Messages.getOkButton(), Messages.getCancelButton(), Messages.getInformationIcon());
                Messages.showMessageDialog(project, String.valueOf(txt), "OK", Messages.getInformationIcon());
                return;
            }
            case "customError": {
                int txt = Messages.showOkCancelDialog("message", "Custom path is not a file. Please check it in Settings(or Other Settings).", Messages.getOkButton(), Messages.getCancelButton(), Messages.getInformationIcon());
                Messages.showMessageDialog(project, String.valueOf(txt), "OK", Messages.getInformationIcon());
                return;
            }
        }

        callback.action(project);
    }

    private static String kot(String s) {
        return "\"" + s + "\"";
    }

    public static void runGitBash(String gitPath) {
        if (gitPath == null || gitPath.length() <= 2) {
            return;
        }

        var disk = gitPath.substring(0, 2);
        String cmd = String.format("%s && cd %s && %s", disk, gitPath,
                kot(gitToolPathAfterAsset()));
        runCommand(cmd);
    }

    public static void runGitDiff(String gitPath, String relativeFile) {
        runGitBashCmds(gitPath, new String[]{"git diff " + relativeFile});
    }

    public static void runGitStatus(String gitPath, String relativeDir) {
        String s;
        if (relativeDir.isEmpty()) {
            s = "git status";
        } else {
            s = "git status " + relativeDir;
        }
        runGitBashCmds(gitPath, new String[]{s});
    }

    private static void runGitBashCmds(String gitPath, String[] extraCmds) {
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
        String cmd = String.format("%s && cd %s && start \"\" %s -c %s", disk, gitPath,
                kot(gitToolPathAfterAsset()),
                kot(gitCmds.toString()));
        runCommand(cmd);
    }

    @Nullable
    static NoGitPathInfo findClosestGitRoot(@Nonnull AnActionEvent event) {
        var thisFile = event.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE);
        if (thisFile == null) {
            // 处理未找到 VirtualFile 的情况（例如弹出提示或日志）
            return null;
        }
        var thisFileStr = thisFile.toString().replace("file://", "");
        var file = new File(thisFileStr);
        String dir = "";
        if (file.exists() && file.isFile()) {
            dir = thisFileStr.substring(0, thisFileStr.length() - file.getName().length());
        } else if (file.exists() && file.isDirectory()) {
            dir = thisFileStr + "/";
        }
        if (dir.isEmpty()) {
            return null;
        }

        File f = new File(dir + "/.git/config");
        boolean isCut = false;
        while(!f.exists()) {
            isCut = true;
            dir = new File(dir).getParent();
            if (dir == null || dir.isBlank()) { //blank return no git pathInfo
                return new NoGitPathInfo(thisFileStr);
            }
            f = new File(dir + "/.git/config");
        }

        PathInfo p = new PathInfo();
        p.path = thisFileStr;
        p.relativePath = isCut ? thisFileStr.substring(dir.length() + 1) : thisFileStr;
        p.gitPath = dir;
        p.isFileOrDirectory = file.isFile();

        return p;
    }
}
