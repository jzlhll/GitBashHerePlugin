package com.allan.openhereplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.allan.openhereplugin.Common.runWindowsCmd;

public class WindowCmdAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        runWindowsCmd();
    }
}