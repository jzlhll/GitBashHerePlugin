package com.allan.openhereplugin;

import com.allan.openhereplugin.config.GitOpenHereSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class WindowCmdAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var thisFile = event.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE);
        if (thisFile == null) {
            return;
        }
        String directory;
        if (thisFile.isDirectory()) {
            directory = thisFile.toString().replace("file://", "");
        } else {
            directory = new File(thisFile.toString().replace("file://", ""))
                    .getParentFile().getAbsolutePath();
        }

        var type = GitOpenHereSettings.getInstance().getState().windowCmdType;
        if (type == GitOpenHereSettings.WINDOW_CMD_TYPE_CMD) {
            openWindowCmd(directory);
        } else if (type == GitOpenHereSettings.WINDOW_CMD_TYPE_POWER_CMD) {
            openWindowPowerShell(directory);
        }
    }

    private void openWindowCmd(String targetDir) {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "start", "cmd.exe", "/k", "cd /d " + targetDir);
            pb.start();
        } catch (IOException e) {
            //
        }
    }

    private void openWindowPowerShell(String targetDir) {
        try {
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", "Start-Process", "powershell",
                    "-ArgumentList", "'-NoExit', '-Command', 'cd " + targetDir + "'");
            pb.start();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        var state = GitOpenHereSettings.getInstance().getState();
        boolean v = Common.isWindow() && state.windowCmdType != GitOpenHereSettings.WINDOW_CMD_TYPE_NO;
        e.getPresentation().setVisible(v);
    }
}
