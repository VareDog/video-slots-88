# 视频槽位 88

网页端管理 88 个视频槽位，安卓电视端遥控器播放。视频存 Cloudflare R2，Worker 提供页面和 API，GitHub Actions 云端构建 APK。

## 架构

```
浏览器 ──上传──> tv.988199.xyz (Cloudflare Worker)
                    ├── 页面：88 槽位管理（内嵌 HTML）
                    ├── API：/api/videos /api/slots/*
                    ├── 元数据：KV (video-slots-88-meta)
                    └── 视频文件：R2 (video-slots-88, APAC)

安卓电视 APK ──读取──> /api/videos + /v/*（公开，无需口令）
```

- 小于 90MB 的视频：浏览器单请求直传 Worker 转存 R2
- 大于 90MB：浏览器按 90MB 分片，走 R2 Multipart Upload
- 封面：上传时浏览器从视频随机抽一帧画到 canvas 生成 JPEG
- 视频播放接口支持 Range 请求（拖动进度条秒切）

## 网页端

地址：https://tv.988199.xyz

- 88 个槽位，空槽位点 `+` 上传；已占用槽位点封面预览播放，点 `⋯` 管理菜单（更换视频 / 编辑标题 / 删除）
- 上传弹窗：选择文件 → 自动填标题（默认取文件名）→ 自动抽随机帧封面 → 进度条 → 完成
- 写操作（上传/更换/编辑/删除）需要访问口令，首次输入后记在浏览器本地

访问口令：`988199tv`（可在 Worker 绑定的环境变量 ACCESS_CODE 中更换）

## 安卓电视端

包名 `com.varedog.videoslots`，最低支持 Android 6.0（API 23），横屏，零第三方依赖。

APK 获取：GitHub Actions（本仓库）运行成功的 Artifacts 里下载 `videoslots-apk`。

操作说明：

| 按键 | 网格页 | 播放页 |
|------|--------|--------|
| 上下左右 | 移动光标选中视频 | - |
| OK / 确认 | 播放选中的视频 | 播放 / 暂停 |
| 菜单键 | 刷新列表 | 勾选「启用连续播放」 |
| 返回键 | - | 退出播放 |

连续播放逻辑（在菜单里勾选开启）：

- 单视频：播完自动循环
- 多视频：按顺序连播，最后一个播完回到第一个
- 关闭时：播完自动退回网格页

网格页只显示已上传的视频，空槽位在电视端隐藏。

## 播放兼容性

安卓 6 的系统播放器对编码有要求，建议上传 **H.264 + AAC 编码的 MP4**。HEVC/H.265 或 mkv/webm 封装可能无法在电视上播放（网页端预览不受限）。

## 本地开发

```bash
cd android
gradle assembleDebug
```

Worker 部署（Cloudflare API）：

```bash
curl -X PUT "https://api.cloudflare.com/client/v4/accounts/<ACC>/workers/scripts/video-slots-88" \
  -H "X-Auth-Email: <EMAIL>" -H "X-Auth-Key: <KEY>" \
  -F 'metadata={"main_module":"worker.js","compatibility_date":"2025-01-01","bindings":[{"type":"r2_bucket","name":"BUCKET","bucket_name":"video-slots-88"},{"type":"kv_namespace","name":"META","namespace_id":"<KV_ID>"},{"type":"secret_text","name":"ACCESS_CODE","text":"<口令>"}]};type=application/json' \
  -F 'worker.js=@worker/worker.js;type=application/javascript+module'
```

## API 一览

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | / | 公开 | 管理网页 |
| GET | /api/videos | 公开 | 已上传视频列表 |
| POST | /api/slots/:n/upload?filename= | 口令 | 单请求上传（≤90MB） |
| POST | /api/slots/:n/multipart/init | 口令 | 开始分片上传 |
| PUT | /api/slots/:n/multipart/part | 口令 | 上传分片 |
| POST | /api/slots/:n/multipart/complete | 口令 | 合并分片 |
| POST | /api/slots/:n/multipart/abort | 口令 | 取消分片 |
| POST | /api/slots/:n/cover | 口令 | 上传封面 JPEG |
| POST | /api/slots/:n | 口令 | 登记/替换槽位内容 |
| PATCH | /api/slots/:n | 口令 | 改标题 |
| DELETE | /api/slots/:n | 口令 | 删除槽位 |
| GET | /v/:key | 公开 | 视频流（支持 Range） |
| GET | /c/:key | 公开 | 封面图 |

槽位号 n 为 1-88。鉴权方式：请求头 `X-Access-Code`。
