pluginManagement {
    repositories {
        // 1. 阿里云镜像放在最前面
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        // 2. 原生源作为备份
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 1. 阿里云镜像放在最前面
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        // 2. 原生源作为备份
        google()
        mavenCentral()
    }
}

rootProject.name = "SmartOutfitApp"
include(":app")