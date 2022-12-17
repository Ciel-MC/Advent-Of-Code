plugins {
    kotlin("jvm") version "1.7.22"
}

repositories {
    mavenCentral()
}

tasks {
    sourceSets {
        main {
            java.srcDirs("src")
        }
    }

    wrapper {
        gradleVersion = "7.6"
    }
    compileKotlin {
        kotlinOptions {
            languageVersion = "1.9"
            incremental = false
            jvmTarget = "18"
            useK2 = true
        }
    }
}
dependencies {
    // Add kotlinx.serialization to the project
    implementation(kotlin("reflect"))
    // Add kotlin coroutines to the project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}