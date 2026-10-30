pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GramPlay"

include(":desktop")
include(":core")
if (!providers.gradleProperty("desktopOnly").orElse("false").get().toBoolean()) include(":app")
