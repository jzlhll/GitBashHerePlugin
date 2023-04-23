package com.allan.openhereplugin;

import javax.annotation.Nonnull;

public interface IFindGitBashPathCallback {
    void action(@Nonnull com.intellij.openapi.project.Project project);
}
