rootProject.name = "jetbrains-just-plugin"

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "1.9.20"
        id("org.jetbrains.changelog") version "2.2.0"
        id("org.jetbrains.grammarkit") version "2022.3.2.2"
        id("org.jetbrains.qodana") version "2023.2.1"
    }
}

plugins {
    id("org.jetbrains.intellij.platform.settings") version "2.11.0"
}
