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

rootProject.name = "AndroidQuickStartApp"
include(":app")
include(":trackingSdk")
project(":trackingSdk").projectDir = file("./trackingSdk/library")
include(":authSdk")
project(":authSdk").projectDir = file("./authSdk/library")
 