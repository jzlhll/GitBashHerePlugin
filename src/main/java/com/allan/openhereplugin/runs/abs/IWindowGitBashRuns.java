package com.allan.openhereplugin.runs.abs;

import com.allan.openhereplugin.bean.Pair;

public interface IWindowGitBashRuns {
    /**
     * 结合custom和系统找到的路径
     */
    Pair findPathExe();

    /**
     * 自己找到的工具path
     */
    String origPathExe();
}