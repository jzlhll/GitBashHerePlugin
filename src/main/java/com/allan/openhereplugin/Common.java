package com.allan.openhereplugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Common {
    private static String foundGitBashPath;
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

    private static String findGitBash() {
        boolean isWin = isWindows();
        if (!isWin) {
            return "notWin";
        }

        if (foundGitBashPath != null) {
            boolean isNoPath = foundGitBashPath.equals(NOT_FOUND_GITBASH_PATH);
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
                foundGitBashPath = path;
                return "ok";
            }
        }

        foundGitBashPath = NOT_FOUND_GITBASH_PATH;
        return "no";
    }

    public static void runCommand(String command) {
        String[] cmdArr = new String[3];
        cmdArr[0] = "cmd";
        cmdArr[1] = "/c";
        cmdArr[2] = command;

        try {
            Runtime.getRuntime().exec(cmdArr, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean assertPath(com.intellij.openapi.project.Project project, IFindGitBashPathCallback callback) {
        if (project == null) {
            return false;
        }

        var ans = findGitBash();

        if ("notWin".equals(ans)) {
            int txt = Messages.showOkCancelDialog("message", "Not support linux/mac.", Messages.getOkButton(), Messages.getCancelButton(), Messages.getInformationIcon());
            Messages.showMessageDialog(project, String.valueOf(txt), "OK", Messages.getInformationIcon());
            return false;
        }

        if ("no".equals(ans)) {
            int txt = Messages.showOkCancelDialog("message", "Cannot found git-bash.exe path, please feedback to https://github.com/jzlhll/GitBashHerePlugin", Messages.getOkButton(), Messages.getCancelButton(), Messages.getInformationIcon());
            Messages.showMessageDialog(project, String.valueOf(txt), "OK", Messages.getInformationIcon());
            return false;
        }

        callback.action(project);
        return true;
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
                kot(foundGitBashPath));
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
        if (gitPath == null || gitPath.length() == 0) {
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
                kot(foundGitBashPath),
                kot(gitCmds.toString()));
        runCommand(cmd);
    }

    @Nullable
    static NoGitPathInfo findClosestGitRoot(@Nonnull AnActionEvent event) {
        var thisFile = event.getDataContext().getData("virtualFile");
        var thisFileStr = thisFile.toString().replace("file://", "");
        var file = new File(thisFileStr);
        String dir = "";
        if (file.exists() && file.isFile()) {
            dir = thisFileStr.substring(0, thisFileStr.length() - file.getName().length());
        } else if (file.exists() && file.isDirectory()) {
            dir = thisFileStr + "/";
        }
        if (dir.length() == 0) {
            return null;
        }

        File f = new File(dir + "/.git/config");
        boolean isCut = false;
        while(!f.exists()) {
            isCut = true;
            dir = new File(dir).getParent();
            if (dir == null || dir.isEmpty() || dir.isBlank()) { //blank return no git pathInfo
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
