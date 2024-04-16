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

rootProject.name = "Amazon Location Sample App"
include(":app")
include(":trackingSdk")
project(":trackingSdk").projectDir = file("./trackingSdk/library")
include(":authSdk")
project(":authSdk").projectDir = file("./authSdk/library")