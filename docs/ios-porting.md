# iOS port plan

GramPlay starts from Spotygram, whose application is written in Kotlin and
Compose. Android and desktop currently compile a shared source directory, but
that directory is not a Kotlin Multiplatform module. There is no iOS app target
yet.

## Current constraints

- Shared application code uses JVM APIs including `java.io.File`, Java
  concurrency collections and atomics, `java.nio`, `java.net`, `org.json`, and
  JVM gzip and cryptography classes. These need multiplatform equivalents or
  platform interfaces before they can compile for iOS.
- `Platform` and `Player` already define useful host boundaries. An iOS host
  still needs implementations for files, preferences, SQLite, networking,
  metadata, URL handling, background transfers, Telegram client, and audio.
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
2. Convert the shared code into a Kotlin Multiplatform module without changing
   Android or desktop behavior.
3. Move JVM-only operations behind platform interfaces and add common
   implementations where appropriate.
4. Add an iOS target and implement its storage, database, metadata, networking,
   Telegram/TDLib, and audio adapters.
5. Add an iOS app target, then verify sign-in, local imports, playback,
   background audio, library sync, and lifecycle behavior on a simulator and
   physical device.

This document records the initial assessment. It does not claim that an iOS
build or any iOS behavior is available yet.
