package com.allan.openhereplugin.bean;

import java.util.Locale;

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

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "PathInfo path: %s gitPath: %s relativePath: %s", path, gitPath, relativePath);
    }
}
