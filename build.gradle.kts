// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// 顶级 build.gradle.kts（Kotlin DSL 正确写法）
buildscript {
    repositories {
        // 阿里云镜像（Kotlin 语法：url = "地址"）
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        google() // 保留安卓官方仓库
    }
    dependencies {
        // 关键：替换「对应版本」为项目实际的 AGP 版本（比如 8.2.0/8.3.0，必须和 Gradle 兼容）
        // AGP 版本查询：https://developer.android.com/studio/releases/gradle-plugin?hl=zh-cn
        classpath("com.android.tools.build:gradle:8.4.0")
    }
}

// 移除 allprojects 仓库配置，统一在 settings.gradle.kts 中管理

// 可选：如果有 clean 任务，保留
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}