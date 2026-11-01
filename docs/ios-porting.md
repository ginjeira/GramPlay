# iOS port plan

GramPlay starts from Spotygram, whose application is written in Kotlin and
Compose. Android and desktop still compile most of the application from a
shared JVM source directory. The new `core` module is Kotlin Multiplatform and
now contains platform-neutral playback contracts and state, the library
catalog and duplicate grouping, track, playlist, source, profile, file, and
download models, audio metadata, playback progress, queue ordering, repeat
normalization, and playback-speed rules for Android, desktop, and iOS targets.
It exports a static `GramPlayCore` framework for iPhone and simulator
architectures. There is no iOS app target yet.

The `iOS core frameworks` GitHub Actions workflow builds the device and Apple
silicon simulator frameworks on macOS whenever the core or its build setup
changes.

## Current constraints

- Shared application code uses JVM APIs including `java.io.File`, Java
  concurrency collections and atomics, `java.nio`, `java.net`, `org.json`, and
  JVM gzip and cryptography classes. These need multiplatform equivalents or
  platform interfaces before they can compile for iOS.
- `Platform` and `Player` define useful host boundaries. Playback contracts,
  state, catalog, and duplicate-grouping logic, audio metadata, progress,
  queue ordering, and playback-speed rules now live in the Kotlin
  Multiplatform common source set; an iOS host still needs implementations for
  files, preferences, SQLite, networking, URL handling, background transfers,
  Telegram client, and actual audio.
- Android currently provides TDLib through a native JNI build. iOS needs a
  separately built and packaged TDLib integration, including its native
  dependencies, lifecycle, and App Store compliance review.
- The current Compose application source depends on Android/JVM APIs and has
  not been tested with Compose Multiplatform's iOS target. UI sharing should be
  evaluated after the core source is portable; a native SwiftUI shell remains
  an alternative.
- iOS background audio, remote controls, file access, privacy disclosures,
  signing, and App Store distribution need dedicated platform work.

## Implementation sequence

1. Establish a GramPlay app identity and keep upstream updates reviewable.
2. Move more platform-neutral contracts and domain logic into `core`, keeping
   Android and desktop behavior intact.
3. Extract JVM-only operations behind platform interfaces, then move suitable
   domain logic into common code using multiplatform libraries.
4. Add an iOS target and implement its storage, database, metadata, networking,
   Telegram/TDLib, and audio adapters.
5. Add an iOS app target, then verify sign-in, local imports, playback,
   background audio, library sync, and lifecycle behavior on a simulator and
   physical device.

This document records the initial assessment. It does not claim that an iOS
build or any iOS behavior is available yet.
