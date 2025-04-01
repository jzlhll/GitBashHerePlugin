package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.File;

public final class Common {
    public static String gitBashPath;

    public static final CommonGitBashRuns gitBashRuns = new CommonGitBashRuns();
    public static final CommonWarpRuns warpRuns = new CommonWarpRuns();

    static final String NOT_FOUND_GITBASH_PATH = "-no-found-git-bash";

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

    public static void assetGitBashPath(Project project, IFindGitBashPathCallback callback) {
        if (project == null) {
            return;
        }

        switch (gitBashRuns.findPath().first) {
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
