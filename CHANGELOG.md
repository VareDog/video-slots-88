# 修改记录 (CHANGELOG)

> 用途：跟踪每次 APK 功能修改。测试不通过需要继续调整时，先看这里了解上次改了什么、改在哪里。

---

## [未发布] 2026-10-20：播放页竖屏视频拉伸修复（待真机验证）

**状态：代码已构建，等待用户真机验证**

### 问题

竖屏（手机竖拍）视频在 TV 上播放时，画面被拉宽铺满全屏，人物变形。横屏视频显示正常。

### 根因

播放页 `PlayerActivity` 使用 `SurfaceView + MediaPlayer`，布局中 SurfaceView 为全屏
（`match_parent`）。MediaPlayer 直接把解码帧拉伸填满整个 Surface：
- 横屏 16:9 视频在 16:9 电视上恰好铺满，看起来正常
- 竖屏 9:16 视频被强行拉宽到全屏 → 变形

### 改动明细（仅 1 个文件，+56 行）

文件：`android/app/src/main/java/com/varedog/videoslots/PlayerActivity.kt`

| 位置 | 改动 |
|------|------|
| import | 新增 `android.media.MediaMetadataRetriever` |
| 字段 | 新增 `lastAspect`（最近一次视频显示尺寸）、`aspectToken`（防过期回调） |
| `playIndex()` | 新增 `setOnVideoSizeChangedListener`，视频尺寸回调时调用 `applyVideoAspect` |
| `swapOrPlay()` | 连续播放预取切换到下一视频后，追加调用 `applyVideoAspect`（预取的 Player 没挂监听，需手动应用） |
| `surfaceChanged()` | 容器尺寸就绪/变化时，用 `lastAspect` 重新应用布局 |
| 新增 `applyVideoAspect()` | 后台线程读视频旋转元数据（90/270 度时交换宽高——手机竖拍视频编码尺寸是横的、靠旋转元数据转竖，必须换算），然后调 `layoutSurface` |
| 新增 `layoutSurface()` | fit-center 居中：按容器和视频比例取较小缩放系数，重设 SurfaceView 的 LayoutParams 宽高 |

布局 XML、Worker（tv.988199.xyz）、管理页、上传逻辑：**均未改动**。

### 修复后预期效果

- 横屏视频：照常铺满全屏
- 竖屏视频：居中显示，左右留黑边，人物不变形
- 连续播放切换到下一个视频后比例同样正确

### 回退方式

- 修改前基线：git tag `backup-v1.1.1-20261020`（已推送到 GitHub）
- 回退命令：`git checkout backup-v1.1.1-20261020 -- android/app/src/main/java/com/varedog/videoslots/PlayerActivity.kt`
- 本地完整备份副本：`/tmp/opencode/video-slots-88-backup/`

### 验证清单（用户真机）

1. 竖屏视频显示正常、不拉伸
2. 横屏视频照常铺满
3. 连续播放开启后，切换视频比例正常
4. 单视频循环播放正常

### 本次提交

- `4baf41c` fix: 播放页按视频宽高比缩放画面，竖屏视频不再拉伸
- `003028f` docs: MEMORY 记录修改前备份要求

### 测试不通过时的调整方向（备查）

- 若竖屏视频希望"拉伸铺满"以外的其他效果（如放大裁切铺满）：改 `layoutSurface()` 中的缩放算法（`minOf` 改 `maxOf` 并配合 SurfaceView 裁切）
- 若个别视频比例仍不对：可能是旋转元数据读取失败（弱网下 `MediaMetadataRetriever` 读 HTTP 元数据超时），可在 `applyVideoAspect()` 失败分支加重试或用固定宽高
