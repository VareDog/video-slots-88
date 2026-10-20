# User Instruction Memory

This file records user instructions, preferences, and teachings for reference in future interactions.

## Format

[User Instruction Summary]
- Date: [YYYY-MM-DD]
- Context: [Mentioned scenario or time]
- Instructions:
  - [Content of user teaching or instruction, described line by line]

### Project Knowledge Entry
Entries discovered by the Agent during task execution should follow this format.

## Entries

[APK 调试标准流程 - 崩溃日志收集方案]
- Date: 2026-09-08
- Context: 用户在 reminder-app 项目中确立，要求以后所有新 Android 项目沿用（本仓库 APK 已内置）
- Category: Troubleshooting & Debugging | Workflow & Collaboration
- Instructions:
  - 新 Android 项目默认内置崩溃收集四件套：CrashHandler（写 filesDir/crash.txt）+ Application onCreate 安装 handler + LauncherActivity 预演入口 + CrashReportActivity（android:process=":crash" 独立进程显示日志）
  - 排查真机闪退优先走"诊断页截图"拿日志，adb logcat 仅作兜底（用户无电脑 adb 环境）
  - crash.txt 读后即删（consume），防止重复弹日志
  - 高频崩溃源：findViewById 的 Kotlin 声明类型必须与 XML 实际类型精确匹配

[用户资产边界]
- Date: 2026-10-17
- Context: 部署 video-slots-88 项目时用户明确强调
- Category: Workflow & Collaboration
- Instructions:
  - GitHub 和 Cloudflare 上只操作本项目新建的资源（video-slots-88 系列），用户其他仓库、Worker、R2 桶、DNS 记录一律不动
  - GitHub token 与 CF 全局 API Key 属用户敏感信息，禁止写入仓库文件

[Project Knowledge Summary]
- Date: 2026-10-17
- Context: Agent 部署 video-slots-88 全栈项目过程中发现
- Category: Operations & Deployment | Build Methods | Troubleshooting & Debugging | Environment Configuration
- Instructions:
  - Worker 部署命令用 CF API multipart PUT（见 README），注意 bindings 中 secret_text ACCESS_CODE 每次部署都必须随 metadata 重发，否则丢失
  - 本 CF 账户 Worker 运行时无 R2 binding createSignedUrl，预签名方案不可用；大文件用 Worker 中转 R2 Multipart（resumeMultipartUpload + uploadPart(request.body)）
  - R2 Multipart 每片最小 5MiB（最后一片除外），complete() 对过小分片抛异常（表现 Worker 1101）；客户端分片定为 90MB
  - 开发沙箱对 *.workers.dev 和 988199.xyz 有 DNS 污染：先 DoH 查真实 IP，再 curl --resolve 强连；GitHub Actions 内可直接 curl 探活（已写入工作流 Check live API 步骤）
  - 子域名 tv.988199.xyz 为本项目 Worker Custom Domain；video.988199.xyz 已被用户其他 R2 桶占用
  - AGP 8.7.3 + Kotlin 2.0.21 + Gradle 8.9 + JDK 17 + compileSdk 35 的 CI 组合已验证可用
  - Kotlin 中 GridView.OnItemClickListener 用 lambda 赋值需显式 SAM 构造器，否则编译报 "An explicit type is required"

[修改代码前必须先备份]
- Date: 2026-10-20
- Context: 修复竖屏视频拉伸时用户多次强调"修改前一定要备份"
- Category: Workflow & Collaboration
- Instructions:
  - 本项目任何代码修改前，必须先完成三重备份：本地完整副本（/tmp/opencode/<项目名>-backup/）+ 远程 git tag（backup-<版本>-<日期>）+ 确认 git 历史可回退
  - 备份完成并向用户报告后才允许动代码；改动遵循最小化原则（只动目标文件）
  - 用户明确要求"功能都正常的部分不要改"，禁止顺手重构或修改无关功能
