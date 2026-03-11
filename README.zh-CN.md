# Kudo

[English](./README.md) | [简体中文](./README.zh-CN.md)

原版 Kudo PWA 的原生 Android 实现。

## 简介

Kudo 是一个游戏化任务应用，包含：

- Habits
- Focus / Inbox 任务
- 奖励商店
- 操作日志
- 本地备份与恢复
- 跟随系统 / 浅色 / 深色主题

## 安装

不需要 Google Play。

1. 打开 GitHub Releases 页面
2. 下载最新的 `Kudo-<版本号>.apk`
3. 如果 Android 提示，允许浏览器或文件管理器安装未知应用
4. 打开 APK 并安装

发布页：`https://github.com/JasperYep/Kudo/releases`

## 构建

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

- Debug APK：`app/build/outputs/apk/debug/app-debug.apk`
- Release APK：`app/build/outputs/apk/release/`

## 自动发布

- 推送 tag 会触发 GitHub Actions
- Actions 会构建签名 `release` APK
- APK 会自动上传到 GitHub Releases

签名配置说明：`docs/RELEASE_SIGNING.md`

## 技术

- Kotlin
- Jetpack Compose
- DataStore
- MPAndroidChart
- GitHub Actions

## 结构

```text
app/src/main/java/com/kudo/app/
├── core/
├── ui/screens/
├── ui/theme/
├── ui/viewmodel/
├── KudoApplication.kt
└── MainActivity.kt
```
