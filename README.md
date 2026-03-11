# Kudo

Native Android build of the original Kudo PWA.  
原版 Kudo PWA 的原生 Android 实现。

## Overview / 简介

- Gameified todo app with habits, Focus/Inbox tasks, rewards store, and logs.  
  带有 Habits、Focus/Inbox、奖励商店和日志的游戏化任务应用。
- Built for direct APK distribution outside Google Play.  
  面向 Google Play 之外的直接 APK 分发。
- Public repository with automated signed release APK builds via GitHub Actions.  
  公开仓库，使用 GitHub Actions 自动构建签名 release APK。

## Features / 功能

- Habits, Focus, Inbox, Store, Log  
  Habits、Focus、Inbox、Store、Log
- Drag reorder, swipe actions, haptics  
  拖拽排序、滑动手势、振动反馈
- Local backup and restore through the system file manager  
  通过系统文件管理器进行本地备份与恢复
- System / Light / Dark theme  
  跟随系统 / 浅色 / 深色主题

## Install / 安装

- Download the latest APK from the GitHub Releases page and install it on your Android device.  
  从 GitHub Releases 页面下载最新 APK，并直接安装到 Android 设备。
- Releases page: `https://github.com/JasperYep/Kudo/releases`  
  发布页：`https://github.com/JasperYep/Kudo/releases`
- If Android blocks installation, allow installs from your browser or file manager and try again.  
  如果 Android 阻止安装，请允许浏览器或文件管理器安装未知应用后重试。

## Build / 构建

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`  
  Debug APK：`app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/`  
  Release APK：`app/build/outputs/apk/release/`

## Release Automation / 自动发布

- Push a tag such as `v1.0.2` to trigger the workflow.  
  推送类似 `v1.0.2` 的 tag 即可触发自动发布。
- GitHub Actions builds a signed **release** APK and uploads it to GitHub Releases.  
  GitHub Actions 会构建签名 **release** APK，并上传到 GitHub Releases。
- Signing setup: `docs/RELEASE_SIGNING.md`  
  签名配置说明：`docs/RELEASE_SIGNING.md`

## Tech / 技术

- Kotlin
- Jetpack Compose
- DataStore
- MPAndroidChart
- GitHub Actions

## Project Structure / 项目结构

```text
app/src/main/java/com/kudo/app/
├── core/
├── ui/screens/
├── ui/theme/
├── ui/viewmodel/
├── KudoApplication.kt
└── MainActivity.kt
```

## Status / 状态

- Active Android project.  
  持续开发中的 Android 项目。
- If you want this repository to be formally open source, add a `LICENSE` file.  
  如果你希望这个仓库在法律意义上成为正式开源项目，请补一个 `LICENSE` 文件。
