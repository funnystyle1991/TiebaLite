pluginManagement {
    repositories {
        // 国内镜像优先，Google 作兜底
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
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
