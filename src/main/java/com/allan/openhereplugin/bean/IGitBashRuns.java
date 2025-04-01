package com.allan.openhereplugin.bean;

public interface IGitBashRuns extends IRuns{
    Pair findPath();
    void runGitBash(String gitPath);
    void runGitDiff(String gitPath, String relativeFile);
    void runGitStatus(String gitPath, String relativeDir);
    void runGitLog(String gitPath, String relativeFile);
}