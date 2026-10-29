package com.allan.openhereplugin;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.PluginId;

/** 在提供官方处理器的 IDE 中启用 Marketplace 上报，并随插件卸载清理注册。 */
public final class MarketplaceErrorReportingService implements Disposable {
    private static final Logger LOG = Logger.getInstance(MarketplaceErrorReportingService.class);

    @SuppressWarnings("TestOnlyProblems")
    public MarketplaceErrorReportingService() {
        try {
            // 官方处理器从 2023.3 起提供，反射加载避免旧版 IDE 链接到不存在的类。
            var handlerClass = Class.forName(
                    "com.intellij.diagnostic.JetBrainsMarketplaceErrorReportSubmitter",
                    true,
                    ErrorReportSubmitter.class.getClassLoader()
            ).asSubclass(ErrorReportSubmitter.class);
            var plugin = PluginManagerCore.getPlugin(PluginId.getId("com.allan.OpenHere"));
            if (plugin == null) {
                LOG.warn("Cannot register Marketplace error reporting: plugin descriptor is unavailable");
                return;
            }

            // 自动上报检查实际类型，必须注册官方实例；插件描述用于正确归属报告。
            var handler = handlerClass.getDeclaredConstructor().newInstance();
            handler.setPluginDescriptor(plugin);
            // 此动态注册接口标注为 TestOnly；为保留旧版兼容而限定使用，并绑定服务生命周期。
            ExtensionPointName.<ErrorReportSubmitter>create("com.intellij.errorHandler")
                    .getPoint().registerExtension(handler, plugin, this);
        } catch (ClassNotFoundException e) {
            // 2022.1～2023.2 没有官方处理器，保留原有行为。
        } catch (ReflectiveOperationException | LinkageError e) {
            LOG.warn("Cannot initialize Marketplace error reporting", e);
        }
    }

    @Override
    public void dispose() {
        // 扩展注册的子 Disposable 由平台先行清理。
    }
}
