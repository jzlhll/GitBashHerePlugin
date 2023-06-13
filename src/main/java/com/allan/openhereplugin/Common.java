package com.allan.openhereplugin;

import com.allan.openhereplugin.beans.Bean;
import com.allan.openhereplugin.beans.DiffsBean;
import com.allan.openhereplugin.beans.PathsBean;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.ui.Messages;

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

    public static void runGitDiff(@Nonnull String gitPath, String relativeFile) {
        runGitBashCmds(gitPath, new String[]{"git diff " + relativeFile});
    }

    public static void runGitStatus(@Nonnull String gitPath, @Nonnull String relativeDir) {
        String s;
        if (relativeDir.isEmpty()) {
            s = "git status";
        } else {
            s = "git status " + relativeDir;
        }
        runGitBashCmds(gitPath, new String[]{s});
    }

    private static void runGitBashCmds(@Nonnull String gitPath, String[] extraCmds) {
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

    private static PathsBean getPathsBean(String basePath, String relativeSourceDirectory) {
        PathsBean bean = new PathsBean();

        var noEndRelativeSourceDirectory = relativeSourceDirectory;
        if (relativeSourceDirectory.endsWith("/")) {
            noEndRelativeSourceDirectory = noEndRelativeSourceDirectory.substring(0, relativeSourceDirectory.length() - 1);
        }

        var cutBackDir = noEndRelativeSourceDirectory;
        while (!new File(basePath + cutBackDir + "/.git/config").exists()) {
            var in = cutBackDir.lastIndexOf("/");
            if (in > 0) {
                cutBackDir = cutBackDir.substring(0, cutBackDir.lastIndexOf("/"));
            } else {
                cutBackDir = "";
                break;
            }
        }
        bean.deepGitDirectoryPath = basePath + cutBackDir;
        bean.relativeToDeepGitPath = (basePath + relativeSourceDirectory).replace(bean.deepGitDirectoryPath, "");
        if (bean.relativeToDeepGitPath.startsWith("/")) {
            bean.relativeToDeepGitPath = bean.relativeToDeepGitPath.substring(1);
        }
        return bean;
    }

    private static DiffsBean getPathsBeanFile(String basePath, String relativeSourceFile) {
        DiffsBean bean = new DiffsBean();
        var in = relativeSourceFile.lastIndexOf("/");
        var relativeToProjectDir = in > 0 ? relativeSourceFile.substring(0, relativeSourceFile.lastIndexOf("/")) : relativeSourceFile;

        var pathsBean = getPathsBean(basePath, relativeToProjectDir);
        bean.deepGitDirectoryPath = pathsBean.deepGitDirectoryPath;
        bean.relativeToDeepGitPathFile = (basePath + relativeSourceFile).replace(bean.deepGitDirectoryPath, "");
        if (bean.relativeToDeepGitPathFile.startsWith("/")) {
            bean.relativeToDeepGitPathFile = bean.relativeToDeepGitPathFile.substring(1);
        }
        return bean;
    }

    /**
     * 从project点击出来的计算出，最近的git目录
     */
    static Bean pathToDirectoryForProject(AnActionEvent event, boolean fileMode) {
        if (fileMode) {
            var thisFile = event.getDataContext().getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
            if (thisFile != null) {
                String thisFileStr = thisFile.toString();
                var basePath = event.getProject().getBasePath();
                if (!basePath.endsWith("/")) {
                    basePath = basePath + "/";
                }
                var relativeSrc = thisFileStr.substring(thisFileStr.indexOf(basePath) + basePath.length());
                return getPathsBeanFile(basePath, relativeSrc);
            }
        } else {
            var thisFile = event.getDataContext().getData("virtualFile");
            var basePath = event.getProject().getBasePath();

            String thisFileStr = thisFile.toString();

            var s = thisFileStr.replace("file://", "");
            var file = new File(s);
            if (file.exists() && file.isFile()) {
                thisFileStr = thisFileStr.substring(0, thisFileStr.length() - file.getName().length());
            } else if (file.exists() && file.isDirectory()) {
                thisFileStr = thisFileStr + "/";
            }
            if (!basePath.endsWith("/")) {
                basePath = basePath + "/";
            }

            var relativeSrc = thisFileStr.substring(thisFileStr.indexOf(basePath) + basePath.length());
            return getPathsBean(basePath, relativeSrc);
        }

        return null;
    }
}
