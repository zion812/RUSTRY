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
        // Add Alibaba Maven mirror as fallback
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/google")
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add Alibaba Maven mirror as fallback
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/google")
    }
}

rootProject.name = "RUSTRY"
include(":app")
