The iOS build workflow places the generated `core.xcframework` in this folder
before invoking Xcode. For a local build, assemble it with
`./gradlew :core:assembleCoreDebugXCFramework`, then copy the resulting
`core.xcframework` from the configured Gradle build directory into this folder.
The framework is generated locally and is not committed to the repository.
