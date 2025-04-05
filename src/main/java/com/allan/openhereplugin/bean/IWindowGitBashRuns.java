package com.allan.openhereplugin.bean;

import com.intellij.openapi.project.Project;

public interface IWindowGitBashRuns {
    /**
     * 结合custom和系统找到的路径
     */
    Pair findPathExe();

    /**
     * 自己找到的工具path
     */
    String origPathExe();

    boolean checkIfCanRun(Project project);
}