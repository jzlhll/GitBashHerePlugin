package com.allan.openhereplugin;
public class PathInfo {
    public static final PathInfo EMPTY = new PathInfo();
    /**
     * self orig path
     */
    public String path;

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
