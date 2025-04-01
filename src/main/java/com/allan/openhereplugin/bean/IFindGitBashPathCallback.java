package com.allan.openhereplugin.bean;

import javax.annotation.Nonnull;

public interface IFindGitBashPathCallback {
    public void action(@Nonnull com.intellij.openapi.project.Project project);
}
