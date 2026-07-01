# TimeMap - 时间地图应用

一个Android应用，结合Google Maps和时间轴滑动条，让用户可以在地图上探索不同历史时期的重要事件和建筑。

## 功能特点

### 核心功能
- 🗺️ **Google Maps集成**：使用Google Maps SDK显示地图
- ⏱️ **时间轴滑动条**：自定义的对数时间轴滑动条，支持公元前到现在的年份选择
- 📍 **历史数据展示**：根据选中的年份，在地图上显示该时期的重要历史事件和建筑
- 🎯 **位置感知**：根据地图当前位置和缩放级别，智能加载附近的历史数据

### 时间轴特点
- **对数刻度显示**：长时间跨度下仍能精确选择年份
- **可配置最早年份**：用户可自定义最早查看的年份（支持公元前）
- **年份标签**：滑动时实时显示选中的年份
- **智能刻度**：根据时间跨度自动调整刻度密度

### 地图标记
- 🔴 **红色标记**：历史事件（战争、政治事件、文化事件等）
- 🟢 **绿色标记**：历史建筑（现存、遗址、已毁等状态）
- 📝 **详细信息窗口**：点击标记查看事件的详细信息，包括年份、描述、类别等

## 截图

（待添加实际应用截图）

## 安装说明

### 前置要求
- Android Studio Arctic Fox (2021.3.1) 或更高版本
- Android SDK 21 (Android 5.0) 或更高版本
- Google Maps API Key

### 配置步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd TimeMap
   ```

2. **获取Google Maps API Key**
   - 详见 [API_KEY_GUIDE.md](API_KEY_GUIDE.md)
   - 按照指南获取并配置API Key

3. **配置API Key**
   - 打开 `gradle.properties` 文件
   - 将 `YOUR_GOOGLE_MAPS_API_KEY_HERE` 替换为你的实际API Key

4. **构建并运行**
   - 在Android Studio中打开项目
   - 连接Android设备或启动模拟器
   - 点击"运行"按钮

## 项目结构

```
TimeMap/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/timemap/
│           │   ├── data/
│           │   │   ├── model/
│           │   │   │   ├── HistoricalEvent.kt       # 历史事件数据模型
│           │   │   │   └── HistoricalBuilding.kt    # 历史建筑数据模型
│           │   │   └── HistoricalDataRepository.kt  # 历史数据仓库
│           │   ├── ui/
│           │   │   ├── MapsActivity.kt              # 主活动
│           │   │   └── CustomInfoWindowAdapter.kt   # 自定义信息窗口
│           │   └── views/
│           │       └── LogarithmicTimelineSlider.kt # 自定义时间轴滑动条
│           ├── res/
│           │   ├── layout/
│           │   │   ├── activity_maps.xml            # 主界面布局
│           │   │   └── custom_info_window.xml      # 标记信息窗口布局
│           │   ├── values/
│           │   │   ├── strings.xml
│           │   │   ├── colors.xml
│           │   │   └── themes.xml
│           │   └── drawable/
│           └── AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── API_KEY_GUIDE.md                           # API Key获取指南
└── README.md
```

## 使用说明

### 基本操作
1. **移动地图**：拖动地图查看不同区域
2. **缩放地图**：双指捏合或使用缩放按钮
3. **选择年份**：拖动底部的时间轴滑动条选择年份
4. **查看事件**：点击地图上的标记查看历史事件或建筑的详细信息
5. **设置最早年份**：在顶部输入框中输入最早年份（支持负数，如-221表示公元前221年），点击"设置"按钮

### 示例数据

应用当前包含以下示例数据：

#### 中国历史事件
- 秦始皇统一中国 (-221)
- 汉朝建立 (-202)
- 唐朝建立 (618)
- 安史之乱 (755-763)
- 宋朝建立 (960)
- 元朝建立 (1271)
- 明朝建立 (1368)
- 清朝建立 (1636)
- 鸦片战争 (1840)
- 辛亥革命 (1911)

#### 世界历史事件
- 罗马帝国建立 (-27)
- 文艺复兴 (1300-1600)
- 美国独立宣言 (1776)
- 法国大革命 (1789-1799)
- 第一次世界大战 (1914-1918)
- 第二次世界大战 (1939-1945)

#### 历史建筑
- 长城 (中国)
- 故宫 (中国)
- 兵马俑 (中国)
- 大雁塔 (中国)
- 布达拉宫 (中国)
- 金字塔 (埃及)
- 帕特农神庙 (希腊)
- 罗马斗兽场 (意大利)
- 巴黎圣母院 (法国)
- 埃菲尔铁塔 (法国)

## 技术栈

- **编程语言**：Kotlin
- **最小SDK**：API 21 (Android 5.0)
- **目标SDK**：API 34 (Android 14)
- **主要依赖**：
  - Google Maps SDK for Android
  - AndroidX
  - Material Components

## 未来增强

### 数据扩展
- [ ] 接入Wikipedia API获取更多历史事件
- [ ] 接入DBpedia获取结构化历史数据
- [ ] 支持用户贡献和上传本地历史数据
- [ ] 添加更多国家和地区的历史数据

### 功能增强
- [ ] 添加搜索功能（搜索地点、事件、年份）
- [ ] 添加收藏功能（收藏感兴趣的事件和建筑）
- [ ] 添加分享功能（分享历史事件到社交媒体）
- [ ] 支持时间动画（自动播放年份变化）
- [ ] 添加历史事件详情页面
- [ ] 支持离线数据下载

### UI/UX改进
- [ ] 添加底部抽屉显示事件列表
- [ ] 添加年份快速选择按钮（如：古代、中世纪、近代等）
- [ ] 改进时间轴滑动条的视觉效果
- [ ] 添加暗黑模式支持
- [ ] 支持不同地图样式（标准、卫星、地形等）

### 性能优化
- [ ] 实现分页加载历史数据
- [ ] 添加缓存机制
- [ ] 优化地图标记聚类显示

## 贡献指南

欢迎贡献代码、报告问题或提出改进建议！

### 如何贡献
1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

### 代码规范
- 使用Kotlin官方代码规范
- 添加必要的注释
- 确保所有公开API都有文档注释
- 运行 `./gradlew ktlintCheck` 检查代码风格

## 许可证

（待添加许可证）

## 联系方式

如有问题或建议，请通过以下方式联系：

- 提交Issue
- 发送邮件至：[your-email@example.com]

## 致谢

- Google Maps SDK for Android
- Android Open Source Project
- 所有贡献者

---

**注意**：本应用仅为演示和学习目的。历史数据的准确性和完整性需要进一步完善。如需用于生产环境，请替换示例数据为可靠的数据源。
