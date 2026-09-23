package com.allan.openhereplugin;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class GitRepositoryToolbarInitializer implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        ActionManager manager = ActionManager.getInstance();
        AnAction toolbar = manager.getAction("MainToolbarLeft");
        AnAction repositoryName = manager.getAction("allan.githere_repository_name");
        if (!(toolbar instanceof DefaultActionGroup) || repositoryName == null) {
            return;
        }
        DefaultActionGroup group = (DefaultActionGroup) toolbar;
        for (AnAction action : group.getChildActionsOrStubs()) {
            if (action == repositoryName) {
                return;
            }
        }
        group.add(repositoryName);
    }
}
