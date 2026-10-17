# DroidTree 🌳

> **High-Performance Storage Space & Disk Visualizer for Android**  
> *Inspired by the desktop speed of WizTree and the sleek, fluid aesthetics of iManager.*

[![Release](https://img.shields.io/github/v/release/aadilahammad86/droidtree?color=blue&logo=github)](https://github.com/aadilahammad86/droidtree/releases)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android%208.0%2B%20(API%2026%2B)-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202025.02.00-blue.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

## ✨ Features

- 📊 **Squarified Treemap Visualizer**: Dynamic hierarchical block visualizer allocating screen area proportional to file and directory sizes on disk.
- 🔄 **Dual-View Inspection Mode**: Inspect visual treemap blocks on top and browse a neatly organized, sortable, and scrollable file/folder list below simultaneously.
- ⚡ **Blazing Fast Scanning**: Optimized multi-threaded filesystem crawler powered by Kotlin Coroutines and structured concurrency.
- 🗂️ **Category Breakdown**: Instant analysis of Media (Images, Videos, Audio), Documents, Archives, and System/Other storage usage.
- 🗑️ **Safe File Deletion & Live Sync**: Includes a detailed file metadata bottom sheet, explicit confirmation safety dialogs, and instant in-memory tree recalculation with MediaStore re-indexing.
- 🎨 **Fluid Material 3 & Apple-Inspired Physics**: Edge-to-edge immersive interface, adaptive status bar & navigation insets, spring motion, and tactile interaction feedback.
- 🛡️ **Cryptographically Signed**: Built with 4096-bit RSA keys and APK Signature Schemes v1, v2, and v3.

---

## 📱 Screenshots & Design Showcase

Explore the interactive mockups, SVG concepts, and design specifications in the [`design/`](design/) directory.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material Design 3](https://m3.material.io)
- **Language**: [Kotlin 2.1](https://kotlinlang.org)
- **Architecture**: Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
- **Dependency Injection**: [Koin](https://insert-koin.io/)
- **Async & Reactive**: Kotlin Coroutines & `StateFlow`
- **Settings Persistence**: [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) Preferences
- **Image & Thumbnail Loading**: [Coil 3](https://coil-kt.github.io/coil/)
- **Networking**: [Retrofit 2](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)

---

## 🚀 Building from Source

### Prerequisites
- Android Studio Ladybug / Meerkat or IntelliJ IDEA
- JDK 21
- Android SDK 35

### Clone & Compile
```bash
git clone https://github.com/aadilahammad86/droidtree.git
cd droidtree

# Build debug APK
./gradlew assembleDebug

# Build signed release APK
./gradlew assembleRelease
```

---

## 🛡️ Play Protect & Sideloading Notice

When sideloading newly published open-source APKs, Android's Google Play Protect may show a routine *"Unrecognized app / Scanning"* notification.

### Why this happens
Google Play Protect uses a cloud-based **reputation scoring** mechanism. New APK releases and newly generated cryptographic certificates do not yet have historical telemetry in Google's cloud database.

### Security Guarantees
- DroidTree is 100% open-source with transparent build scripts.
- Release binaries are compiled directly by GitHub Actions runners and signed with full v1, v2, and v3 APK Signature Schemes.
- SHA-256 checksums are published with every GitHub Release for independent verification.

If prompted during installation, tap **"Install anyway"** or submit the APK hash via the [Google Play Protect Developer Appeal Portal](https://support.google.com/googleplay/android-developer/contact/protectappeals) to fast-track global cloud reputation indexing.

---

## 📄 License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
