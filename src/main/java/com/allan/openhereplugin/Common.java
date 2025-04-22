package com.allan.openhereplugin;

import com.allan.openhereplugin.bean.*;
import com.allan.openhereplugin.runs.abs.IGitBashRuns;
import com.allan.openhereplugin.runs.abs.IWarpRuns;
import com.allan.openhereplugin.runs.abs.IWindowGitBashRuns;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public final class Common {
    @Nullable
    public static final IGitBashRuns gitBashRunner = IGitBashRuns.create();
    public static boolean isWindow() {
        return gitBashRunner instanceof IWindowGitBashRuns;
    }

    public static final IWarpRuns warpRunner = IWarpRuns.create();

    public static final String SYSTEM_WINDOWS = "win";
    public static final String SYSTEM_MAC = "mac";

    @Nullable
    public static String supportSystem() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows系统
            return SYSTEM_WINDOWS;
        } else if (os.contains("mac")) {
            // macOS系统
            return SYSTEM_MAC;
        }
        return null;
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
