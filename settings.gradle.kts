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
        maven("https://jitpack.io")
        // 荣耀随心握 SDK（底栏/悬浮键跟手）
        maven("https://developer.hihonor.com/repo")
    }
}

rootProject.name = "TiebaLite"
include(":app")
include(":macrobenchmark")
include(":material-color-utilities")
include(":placeholder")
