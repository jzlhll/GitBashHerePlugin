package com.allan.openhereplugin.runs.abs;

import com.intellij.openapi.project.Project;

public interface IRuns {
    void checkIfCanRun(Project project, Runnable canRunBlock);
}