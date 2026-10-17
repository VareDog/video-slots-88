# 技术设计：视频槽位 88

## 架构

- 单 Worker（video-slots-88）承载网页 HTML + REST API
- R2 桶 video-slots-88（APAC）存视频与封面；KV video-slots-88-meta 存槽位元数据（key: slot:1..slot:88）
- 自定义域 tv.988199.xyz 绑定 Worker
- 安卓 APK（com.varedog.videoslots）零第三方依赖，minSdk 23，TV 遥控器 D-pad 交互

## 关键决策

1. 上传通道：createSignedUrl 在当前运行时不可用，采用 Worker 中转流式直传（≤90MB 单请求）+ R2 Multipart 分片（>90MB，90MB/片）。同源无 CORS 依赖。
2. 封面抽取：浏览器端 video 元素 seek 到 10%-85% 随机点，canvas 导出 JPEG（≤640px 宽，质量 0.82），失败时槽位用占位图。
3. 视频播放：/v/:key 读 Range 头，R2 按 offset/length 取段返回 206，支持拖动进度。
4. 电视端网格只含已占用槽位；封面 Bitmap LruCache 12MB，按 URL 缓存。
5. 崩溃收集四件套（沿用用户既有规范）：CrashHandler 写 filesDir/crash.txt + LauncherActivity 预检消费 + CrashReportActivity 独立 :crash 进程显示。
6. 播放器用框架 VideoView：Android 6 兼容性最好；HEVC 等新编码不支持属预期，README 中提示 H.264 MP4。
7. 访问口令 ACCESS_CODE 存 Worker secret 绑定，头部 X-Access-Code 校验；读接口全部公开供电视端使用。

## 资源清单（本次新建）

- GitHub 仓库 VareDog/video-slots-88
- R2 桶 video-slots-88
- KV namespace video-slots-88-meta（a00a80a2db41439295bcfe15ccacbc00）
- Worker video-slots-88
- 自定义域 tv.988199.xyz
