package com.allan.openhereplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/** 在项目启动时初始化应用级上报服务，多个项目共用同一次注册。 */
public final class MarketplaceErrorReportingInitializer implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        ApplicationManager.getApplication().getService(MarketplaceErrorReportingService.class);
    }
}
