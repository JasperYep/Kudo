# Kudo

[English](./README.md) | [简体中文](./README.zh-CN.md)

Native Android build of the original Kudo PWA.

## Overview

Kudo is a gameified todo app with:

- Habits
- Focus / Inbox tasks
- Rewards store
- Activity log
- Local backup and restore
- System / Light / Dark theme

## Install

You do not need Google Play.

1. Open the GitHub Releases page
2. Download the latest `Kudo-<version>.apk`
3. Allow installs from your browser or file manager if Android asks
4. Open the APK and install it

Releases: `https://github.com/JasperYep/Kudo/releases`

## Build

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/`

## Release Automation

- Pushing a tag triggers GitHub Actions
- Actions builds a signed `release` APK
- The APK is uploaded to GitHub Releases

Signing setup: `docs/RELEASE_SIGNING.md`

## Tech

- Kotlin
- Jetpack Compose
- DataStore
- MPAndroidChart
- GitHub Actions

## Structure

```text
app/src/main/java/com/kudo/app/
├── core/
├── ui/screens/
├── ui/theme/
├── ui/viewmodel/
├── KudoApplication.kt
└── MainActivity.kt
```
