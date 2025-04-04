package com.allan.openhereplugin.bean;

public interface IWindowRuns {
    /**
     * 结合custom和系统找到的路径
     */
    Pair findPathExe();

    /**
     * 自己找到的工具path
     */
    String origPathExe();
}