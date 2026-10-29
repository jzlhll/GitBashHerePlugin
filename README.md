# GitBashHerePlugin
Open git bash from here in IDEA (androidStudio, IDEA ...)

This is for who like gitBash but not IDEA's git tools.
It can directly open the git-bash.exe/warp.exe cmd for me.

For Details:
Please visit https://plugins.jetbrains.com/plugin/21517-gitbashopenhere to see what I can do.

## 异常与卡死报告

插件最低支持版本仍为 IntelliJ Platform 2022.1（build 221）。打开项目时，插件在提供
`JetBrainsMarketplaceErrorReportSubmitter` 的 IDE（2023.3 起）中注册官方 Marketplace
上报处理器；应用级服务确保多个项目只注册一次，并在插件卸载时自动移除注册。
2022.1～2023.2 跳过上报注册，其余功能保持原有行为。

兼容层使用平台标注为 `TestOnly` 的动态扩展注册接口。发布前需在新旧 IDE 中验证加载、
上报入口和卸载；后续升级平台时需复核该接口兼容性。

报告由 IDE 的错误报告流程发送，是否自动上报以及可收集的诊断信息取决于 IDE 版本、
产品能力和用户设置。维护者可在 Marketplace 的 Exceptions 和 Freezes 页面查看收到的报告。
