pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        // Add reliable mirror repositories as fallback
        maven("https://repo.spring.io/milestone") {
            name = "SpringMilestone"
        }
        // JitPack as additional fallback
        maven("https://jitpack.io") {
            name = "JitPack"
        }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add reliable mirror repositories as fallback
        maven("https://repo.spring.io/milestone") {
            name = "SpringMilestone"
        }
        // JitPack as additional fallback
        maven("https://jitpack.io") {
            name = "JitPack"
        }
    }
}

rootProject.name = "RUSTRY"
include(":app")
