# 测试指南 - 搜索附近景点功能

## 功能说明

新增功能：点击地图上的"搜索附近景点"按钮（右下角浮动按钮），可以搜索当前地图位置附近的旅游景点，并将景点照片显示在地图上。

---

## 测试步骤

### 1. 获取Google Maps API Key

**必须完成这一步，否则无法使用地图和搜索功能！**

详细步骤请参考：`API_KEY_GUIDE.md`

**快速步骤**：
1. 访问 [Google Cloud Console](https://console.cloud.google.com/)
2. 创建项目
3. 启用以下API：
   - **Maps SDK for Android** (必需)
   - **Places API** (必需，用于搜索景点)
4. 创建API凭据（API Key）
5. 配置API Key到项目（见下一步）

---

### 2. 配置API Key到项目

#### 方法1：使用gradle.properties（推荐）

1. 打开项目根目录的 `gradle.properties` 文件
2. 将 `YOUR_GOOGLE_MAPS_API_KEY_HERE` 替换为你的实际API Key：
   ```properties
   GOOGLE_MAPS_API_KEY=你的实际API_Key
   ```

#### 方法2：使用local.properties（更安全）

1. 创建或编辑 `local.properties` 文件（此文件已被.gitignore排除）
2. 添加以下内容：
   ```properties
   GOOGLE_MAPS_API_KEY=你的实际API_Key
   ```
3. 修改项目根目录的 `build.gradle.kts`，在 `android` 块中添加读取逻辑（如果还没有）

---

### 3. 编译并运行应用

#### 使用Android Studio：
1. 打开Android Studio
2. 打开项目：`/Users/zhuxiaodong/Documents/GitRepo/AnyTAnyW`
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击"运行"按钮 ▶️

#### 使用命令行：
```bash
cd /Users/zhuxiaodong/Documents/GitRepo/AnyTAnyW
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

### 4. 测试搜索附近景点功能

#### 测试场景1：搜索北京附近的景点

1. 启动应用
2. 地图会默认显示北京位置
3. **点击右下角的搜索按钮**（圆形按钮，带搜索图标）
4. 应用会搜索北京附近的旅游景点
5. 等待几秒钟，景点标记会出现在地图上
6. **点击标记**查看景点详情（名称、地址、评分等）

**预期结果**：
- 地图上显示蓝色标记（表示现代景点）
- 如果景点有照片，标记会显示照片
- 底部信息栏会显示找到的景点数量

---

#### 测试场景2：搜索其他城市附近的景点

1. 启动应用
2. **拖动地图**到你想搜索的城市（如：上海、西安、纽约等）
3. 点击右下角的搜索按钮
4. 应用会搜索当前地图位置附近的景点

**预期结果**：
- 地图上显示该城市附近的景点
- 可以结合时间轴使用（选择不同年份）

---

#### 测试场景3：结合时间轴使用

1. 启动应用
2. 拖动底部的时间轴滑动条，选择不同年份（如：1400年、1900年、2024年）
3. 点击搜索按钮
4. 观察不同年份下显示的景点

**说明**：
- 当前实现中，时间轴主要用于显示历史建筑/事件
- 搜索景点功能会调用Google Places API，返回当前存在的景点
- 可以扩展功能：根据选择的年份过滤景点（例如：只显示该年份已存在的景点）

---

### 5. 验证日志输出

在Android Studio的**Logcat**中查看日志：

```
// 搜索开始时
D/PlacesApiService: Searching nearby attractions...

// 搜索成功时
D/PlacesApiService: Found X attractions

// 搜索失败时
E/PlacesApiService: API request failed: ...
E/PlacesApiService: Network error...
```

---

## 常见问题排查

### 问题1：点击搜索按钮后没有反应

**可能原因**：
1. API Key未正确配置
2. 未启用Places API
3. 网络未连接

**解决方法**：
1. 检查 `gradle.properties` 中的API Key是否正确
2. 在Google Cloud Console中确认已启用 **Places API**
3. 检查设备网络连接

---

### 问题2：地图显示空白

**可能原因**：
1. API Key无效
2. 未启用Maps SDK for Android
3. 应用签名不匹配（如果配置了应用限制）

**解决方法**：
1. 检查API Key是否正确
2. 在Google Cloud Console中确认已启用 **Maps SDK for Android**
3. 暂时移除API Key的应用限制，测试是否是签名问题

---

### 问题3：搜索返回空结果

**可能原因**：
1. 当前位置附近没有旅游景点
2. Places API未启用
3. API配额用尽

**解决方法**：
1. 拖动地图到大城市（如：北京、上海、纽约等）
2. 确认已启用Places API
3. 检查Google Cloud Console的配额使用情况

---

### 问题4：照片无法显示

**可能原因**：
1. 景点没有照片
2. 照片下载失败
3. API Key权限不足

**解决方法**：
1. 这是正常的，不是所有景点都有照片
2. 检查网络连接
3. 确认API Key有访问Places Photo API的权限

---

## 功能扩展建议

当前实现是基础版本，可以进一步扩展：

### 1. 根据年份过滤景点
```kotlin
// 在PlacesApiService中添加参数
suspend fun searchNearbyAttractions(
    lat: Double,
    lng: Double,
    year: Int,  // 添加年份参数
    radius: Int = 5000
): List<PlaceResult>
```

### 2. 添加景点详情页面
- 点击标记后，打开一个新页面显示景点详细信息
- 包括：评分、用户评论、开放时间、联系方式等

### 3. 添加搜索历史
- 保存用户的搜索记录
- 支持快速重新搜索

### 4. 优化照片显示
- 使用Coil或Glide库异步加载照片
- 添加照片缓存机制
- 支持点击标记后显示大图

---

## 下一步

测试成功后，你可以：

1. **扩展历史数据**：在 `HistoricalDataRepository.kt` 中添加更多历史事件和建筑
2. **优化UI**：改进时间轴滑动条的样式和交互
3. **发布应用**：配置签名证书，发布到Google Play商店
4. **获取更多数据**：接入Wikipedia API、DBpedia等公开数据源

---

## 需要帮助？

如果遇到问题，可以：
1. 查看 `API_KEY_GUIDE.md` 获取详细的API Key配置指南
2. 查看 `README.md` 了解项目结构和功能说明
3. 在Google Cloud Console中查看API调用日志和错误详情

祝测试顺利！ 🎉
