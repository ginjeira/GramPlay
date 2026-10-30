plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    jvm()
    listOf(iosArm64(), iosSimulatorArm64(), iosX64()).forEach { target ->
        target.binaries.framework {
            baseName = "GramPlayCore"
            isStatic = true
        }
    }
}
