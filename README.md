# Kudo Android - Gameified Todo App

**原生 Android 版本 - 基于 Jetpack Compose**

---

## 📱 应用简介

Kudo 是一个游戏化的任务管理应用，结合了：
- ✅ Todo 任务管理
- 🎯 习惯养成
- 💰 金币经济系统
- 🎁 奖励商店
- 📈 成长统计

---

## 📦 安装 APK

普通用户不需要 Android Studio，也不需要 Google Play。

1. 打开 GitHub 的 **Releases** 页面
2. 下载最新版本的 `Kudo-<版本号>.apk`
3. 在 Android 设备上允许浏览器或文件管理器安装未知来源应用
4. 打开下载好的 APK 并安装

---

## 🚀 发布自动化

- 仓库保持公开并使用开源许可证
- 每次推送新 tag，GitHub Actions 都会自动构建 **signed release APK**
- 构建产物会自动附加到 GitHub Releases
- 签名 keystore 通过 GitHub Actions secrets 保存，不进入仓库

签名与发布配置说明见：`docs/RELEASE_SIGNING.md`

---

## 🚀 快速开始

### 前提条件

1. **Android Studio** (推荐：最新版)
   - 下载地址：https://developer.android.com/studio

2. **JDK 17** (Android Studio 内置)

3. **Android SDK**
   - API 26 (Android 8.0) 或更高

---

### 安装步骤

#### 1. 打开项目

```
File → Open → 选择 kudo-android 文件夹
```

#### 2. 等待 Gradle 同步

首次打开需要下载依赖，约 2-5 分钟。

#### 3. 构建 APK

```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

#### 4. 获取 APK 文件

```
app/build/outputs/apk/debug/app-debug.apk
```

---

### 安装到手机

#### 方法 1：USB 调试
```
1. 手机开启开发者选项
2. 开启 USB 调试
3. 点击 Android Studio 的 Run 按钮
```

#### 方法 2：直接安装 APK
```
1. 将 APK 发送到手机
2. 允许"未知来源"安装
3. 点击安装
```

---

## 📁 项目结构

```
app/src/main/java/com/kudo/app/
├── data/                    # 数据层
│   ├── entity/             # Room 实体
│   ├── dao/                # 数据访问对象
│   ├── repository/         # 数据仓库
│   └── KudoDatabase.kt     # 数据库
├── domain/                  # 领域层
│   ├── model/              # 数据模型
│   └── GameMechanics.kt    # 游戏机制
├── ui/                      # UI 层
│   ├── theme/              # 主题系统
│   └── screens/            # 页面
└── MainActivity.kt          # 主 Activity
```

---

## 🎨 设计特点

### UI 还原
- 100% 还原 PWA 版本设计
- Material 3 Design
- 深色/浅色主题支持

### 性能优化
- Jetpack Compose (声明式 UI)
- Room Database (本地存储)
- Kotlin Coroutines (异步操作)
- 120fps 动画支持

### 原生能力
- 精准 Haptic 反馈
- 硬件加速动画
- 原生手势处理
- 后台数据同步

---

## 🎮 核心功能

### 任务系统
- **Focus 列表**：重要任务
- **Inbox 列表**：临时任务
- 滑动切换列表
- 拖拽排序

### 习惯养成
- 长按 1.5 秒充能
- 渐进式振动反馈
- 完成次数统计

### 游戏化机制
- 完成任务 → 获得金币
- 金币 → 商店兑换奖励
- 经验值 → 提升等级
- 动态倍数加成

### 数据统计
- 收支趋势图
- 收支比率
- 历史最高金币

---

## 🔧 技术栈

| 组件 | 技术 |
|------|------|
| UI | Jetpack Compose |
| 架构 | MVVM |
| 数据库 | Room |
| 异步 | Kotlin Coroutines |
| 导航 | Navigation Compose |
| 主题 | Material 3 |
| 图表 | MPAndroidChart |

---

## 📊 性能指标

| 指标 | 目标 |
|------|------|
| 冷启动 | < 0.5s |
| 列表滚动 | 120fps |
| 动画帧率 | 120fps |
| Haptic 延迟 | < 10ms |
| 安装包 | < 30MB |
| 内存占用 | < 100MB |

---

## 🐛 已知问题

- [ ] 启动屏待添加
- [ ] 数据备份功能待完善
- [ ] 通知系统待实现

---

## 📝 待开发功能

- [ ] 成就系统
- [ ] 桌面小组件
- [ ] 生物识别
- [ ] 云同步
- [ ] 更多主题

---

## 🤝 贡献指南

这是个人项目，但欢迎反馈和建议！

---

## 📄 许可证

MIT License

---

## 📞 联系方式

有任何问题或建议，欢迎联系！

---

**版本**: 1.0.0  
**最后更新**: 2026-03-04
