# iOS port plan

GramPlay starts from Spotygram, whose application is written in Kotlin and
Compose. Android and desktop still compile most of the application from a
shared JVM source directory. The new `core` module is Kotlin Multiplatform and
now contains platform-neutral playback contracts and state, authentication
state, storage, preferences and Telegram client interfaces, the library catalog
and duplicate grouping, track, playlist, source, profile, file and download
models, audio metadata, playback progress, queue ordering, repeat normalization,
and playback-speed rules for Android, desktop, and iOS targets. It exports a
static `GramPlayCore` framework for iPhone and simulator architectures. A first
SwiftUI iOS app target imports audio from Files, keeps copies in the app's local
Music folder, and provides a basic playback queue with previous, play/pause,
next, seeking, playback speed, and lock-screen/headphone controls. This is an
initial local playback slice, not yet a Telegram-connected GramPlay client.

The `iOS core frameworks` GitHub Actions workflow builds the device and Apple
silicon simulator XCFramework on macOS, links it into the Swift app, builds the
app for the iOS simulator, and publishes `core.xcframework` as a short-lived
artifact. The framework inside the bundle is named `GramPlayCore.framework`.

The first native host adapter, `IosPreferences`, stores the shared settings
contract in an iOS `NSUserDefaults` suite.

## Current constraints

- Shared application code uses JVM APIs including `java.io.File`, Java
  concurrency collections and atomics, `java.nio`, `java.net`, `org.json`, and
  JVM gzip and cryptography classes. These need multiplatform equivalents or
  platform interfaces before they can compile for iOS.
- `Platform` and `Player` define useful host boundaries. Playback contracts,
  storage, preferences and Telegram interfaces, state and catalog models,
  duplicate-grouping logic, audio metadata, progress, queue ordering, and
  playback-speed rules now live in the Kotlin Multiplatform common source set.
  iOS preferences use `NSUserDefaults`; the host still needs implementations
  for files, SQLite, networking, URL handling, background transfers, Telegram
  client, and actual audio.
- Android currently provides TDLib through a native JNI build. iOS needs a
  separately built and packaged TDLib integration, including its native
  dependencies, lifecycle, and App Store compliance review.
- The current Compose application source depends on Android/JVM APIs and has
  not been tested with Compose Multiplatform's iOS target. The initial app uses
  SwiftUI; sharing more UI can be evaluated after its host dependencies are
  portable.
- iOS background audio, remote controls, file access, privacy disclosures,
  signing, and App Store distribution need dedicated platform work.

## Implementation sequence

1. Establish a GramPlay app identity and keep upstream updates reviewable.
2. Move more platform-neutral contracts and domain logic into `core`, keeping
   Android and desktop behavior intact.
3. Extract JVM-only operations behind platform interfaces, then move suitable
   domain logic into common code using multiplatform libraries.
4. Extend the SwiftUI app and implement its storage, database, metadata, networking,
   Telegram/TDLib, and audio adapters.
5. Verify sign-in, local imports, playback,
   background audio, library sync, and lifecycle behavior on a simulator and
   physical device.

The current iOS simulator build and local audio playback are an initial slice;
Telegram sign-in, synchronized library access, background controls, and device
validation are still outstanding.
