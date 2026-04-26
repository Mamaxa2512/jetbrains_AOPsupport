plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.15.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
}

group = "org.example"
version = "1.1.0-beta.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    jvmToolchain(21)
}

ktlint {
    filter {
        include("**/*.kts")
    }
}

tasks.named("ktlintMainSourceSetCheck") {
    enabled = false
}

tasks.named("ktlintTestSourceSetCheck") {
    enabled = false
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.1")

    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        bundledPlugin("com.intellij.java")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

tasks.test {
    useJUnitPlatform()
}

intellijPlatform {
    pluginVerification {
        ides {
            recommended()
        }
    }
}
