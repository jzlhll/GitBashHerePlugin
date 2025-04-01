package com.allan.openhereplugin.bean;
public class PathInfo extends NoGitPathInfo {
    public static final PathInfo EMPTY = new PathInfo();

    /**
     * find upper gitPath
     */
    public String gitPath;

    /**
     *  relative to gitPath
     */
    public String relativePath;

    /**
     * true is file; false is directory. null is no file.
     */
    public Boolean isFileOrDirectory = null;
}
