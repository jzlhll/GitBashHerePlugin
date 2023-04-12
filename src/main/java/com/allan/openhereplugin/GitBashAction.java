package com.allan.openhereplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitBashAction extends AnAction {
    private static String foundGitBashPath;
    private static final String NOT_FOUND_GITBASH_PATH = "-no-found-git-bash";

    private static boolean isWindows() {
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
        String[] listOfPresetsGitBash = {
                "C:\\Program Files\\Git\\git-bash.exe",
                "C:\\Program Files (x86)\\Git\\git-bash.exe",
                "D:\\Program Files\\Git\\git-bash.exe",
                "D:\\Program Files (x86)\\Git\\git-bash.exe",
        };
        for (var path : listOfPresetsGitBash) {
            if (Files.exists(Path.of(path))) {
                return path;
            }
        }
        return NOT_FOUND_GITBASH_PATH;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        var project = event.getProject();
        if (isWindows() && project != null) {
            if (foundGitBashPath == null) {
                foundGitBashPath = findGitBash();
            }

            var projectPath = project.getBasePath();
            if (foundGitBashPath != null && !foundGitBashPath.equals(NOT_FOUND_GITBASH_PATH)) {
                var disk = projectPath.substring(0, 2);
                runCommand(disk + " && cd " + projectPath + " && (start /b " + "\"" + projectPath + "\"" + " \"" + foundGitBashPath + "\")");
            }
        } else {
            int txt = Messages.showOkCancelDialog("message", "Not support linux/mac.", Messages.getOkButton(), Messages.getCancelButton(), Messages.getInformationIcon());
            Messages.showMessageDialog(project, String.valueOf(txt), "OK", Messages.getInformationIcon());
        }
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
}
