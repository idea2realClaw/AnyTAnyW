# Google Maps API Key 获取指南

## 为什么需要API Key？

TimeMap应用使用Google Maps SDK来显示地图和获取地理位置信息。要使用这些功能，你需要一个有效的Google Maps API Key。

## 获取API Key的步骤

### 1. 创建Google Cloud项目

1. 访问 [Google Cloud Console](https://console.cloud.google.com/)
2. 点击项目选择器（顶部导航栏）
3. 点击"新建项目"
4. 输入项目名称（例如："TimeMap"）
5. 点击"创建"

### 2. 启用必要的API

在项目创建后，你需要启用以下API：

1. 在Google Cloud Console中，打开左侧菜单
2. 选择"API和服务" > "库"
3. 搜索并启用以下API：
   - **Maps SDK for Android** (必需)
   - **Places API** (可选，用于未来扩展)
   - **Geocoding API** (可选，用于未来扩展)

### 3. 创建API凭据

1. 在Google Cloud Console中，打开左侧菜单
2. 选择"API和服务" > "凭据"
3. 点击"创建凭据" > "API密钥"
4. 复制生成的API密钥

### 4. 配置API密钥（推荐）

为了提高安全性，建议对API密钥进行限制：

1. 在"凭据"页面，点击你刚创建的API密钥
2. 在"应用限制"部分，选择"Android应用"
3. 点击"添加应用"，输入你的应用的包名和SHA-1证书指纹：
   - 包名：`com.example.timemap`
   - SHA-1指纹：运行以下命令获取（需要先生成签名证书）
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
4. 在"API限制"部分，选择"限制密钥"，只勾选你启用的API
5. 点击"保存"

### 5. 在项目中配置API Key

有两种方式配置API Key：

#### 方式1：直接编辑gradle.properties（推荐用于开发）

1. 打开项目根目录的 `gradle.properties` 文件
2. 将 `YOUR_GOOGLE_MAPS_API_KEY_HERE` 替换为你的实际API Key：
   ```
   GOOGLE_MAPS_API_KEY=你的实际API_Key
   ```

#### 方式2：使用local.properties（更安全）

1. 创建或编辑 `local.properties` 文件（此文件已被.gitignore排除，不会提交到Git）
2. 添加以下内容：
   ```properties
   GOOGLE_MAPS_API_KEY=你的实际API_Key
   ```
3. 修改项目根目录的 `build.gradle.kts`，在 `android` 块中添加：
   ```kotlin
   defaultConfig {
       ...
       manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = localProperties.getProperty("GOOGLE_MAPS_API_KEY", "")
   }
   ```

## 测试API Key

配置完成后，运行应用：

1. 连接Android设备或启动模拟器
2. 在Android Studio中点击"运行"按钮
3. 如果地图正常显示，说明API Key配置成功
4. 如果地图显示空白或报错，检查：
   - API Key是否正确
   - 必要的API是否已启用
   - 应用限制是否正确配置

## 常见问题

### 1. 地图显示空白

- 检查API Key是否正确配置
- 检查是否启用了"Maps SDK for Android"
- 检查Logcat中的错误信息

### 2. "API key not found"错误

- 确保 `gradle.properties` 中的API Key格式正确（没有多余空格或换行）
- 确保 `AndroidManifest.xml` 中的meta-data配置正确

### 3. 模拟器上无法显示地图

- 确保模拟器有Google Play服务
- 或使用真机进行测试

## 安全提示

- **不要**将包含真实API Key的代码提交到公开的Git仓库
- **不要**在客户端代码中硬编码API Key
- 使用API密钥限制来减少滥用风险
- 考虑使用代理服务器来隐藏API Key（生产环境）

## 费用说明

Google Maps SDK for Android在以下情况下是免费的：

- 应用未发布到应用商店
- 或每月活跃用户数少于1000

如果应用发布且用户数超过限制，可能需要付费。详情请查看 [Google Maps Platform 定价](https://cloud.google.com/maps-platform/pricing)。

## 获取帮助

如果遇到问题，可以：

1. 查看 [Google Maps SDK for Android 文档](https://developers.google.com/maps/documentation/android-sdk/overview)
2. 在 [Google Maps Platform 问题排查器](https://developers.google.com/maps/gmp-troubleshooting) 中查找解决方案
3. 在Stack Overflow上搜索相关问题

---

**注意**：本应用当前使用本地示例数据。要获取更多历史数据，你可以：
1. 扩展 `HistoricalDataRepository.kt` 中的示例数据
2. 接入公开的历史数据API（如Wikipedia API、DBpedia等）
3. 使用Google Places API获取地点信息

详见 `DATA_EXTENSION_GUIDE.md`（待创建）
