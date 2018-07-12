import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.2.51"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", kotlin_version))
    }
}

plugins {
    java
}

apply {
    plugin("kotlin")
}

val root = this

subprojects {
    plugins {
        java
    }

    apply {
        plugin("kotlin")
    }

    val kotlin_version: String by root.extra

    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
        maven(url="https://kotlin.bintray.com/kotlinx")
    }

    dependencies {
        "compile"(kotlin("stdlib-jdk8", kotlin_version))
        "testCompile"("com.nhaarman", "mockito-kotlin-kt1.1", "1.5.0")
        "testImplementation"("org.junit.jupiter", "junit-jupiter-api", "5.2.0-M1")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}