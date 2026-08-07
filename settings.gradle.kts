pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
}

stonecutter {
    create(rootProject) {
        // Only 1.21.11 is actively built and published. The other versions are parked, not
        // deleted: their versions/<mc>/gradle.properties and every //? if <mc> block in the
        // source are still here, so re-enabling one is just adding it back to this list.
        //
        // Caveat: code written while a version is parked won't have stonecutter branches for
        // it, so expect to fix compile errors in anything new when you re-add one.
        //
        // Parked: "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5",
        //         "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10"
        versions("1.21.11")
    }
}
