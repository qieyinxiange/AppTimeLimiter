# AppTimeLimiter（安卓 MVP）

一个本地运行的 Android App，用于给指定应用设置“每日使用时长上限”。

## 功能
- 从手机可启动应用中选择 App
- 为每个 App 设置每日 1–1440 分钟限制
- 查看今天已使用 / 限制时长
- 达到上限后，再打开目标 App 时显示拦截页
- 规则只保存在本机 SharedPreferences，不上传服务器

## 运行
1. 用 Android Studio 打开本目录。
2. 如果 IDE 提示升级 Android Gradle Plugin/Gradle，可按 IDE 建议升级。
3. 连接 Android 8.0+ 真机并运行。
4. 首次使用：
   - 点击“开启使用情况访问权限”，允许 App 限时器读取使用统计；
   - 点击“开启无障碍服务”，手动启用 App 限时器；
   - 选择一个 App，输入每天允许分钟数并保存。

## 说明
此项目是可运行的 MVP，不是系统级设备管理器。用户如果关闭权限、关闭无障碍服务或卸载应用，限制就会失效。

若要做成家长控制/企业管理产品，应进一步使用 Android Enterprise / Device Owner 等设备管理能力，并遵守 Google Play 对 Accessibility API 的政策要求。
