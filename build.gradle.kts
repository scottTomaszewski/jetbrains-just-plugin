import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
    id("org.jetbrains.qodana")
    id("org.jetbrains.grammarkit")
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate(properties("platformVersion"))

        bundledPlugins(listOf("com.jetbrains.sh", "JavaScript"))
        plugins(listOf("PsiViewer:242.4697"))

        instrumentationTools()
    }
}

changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    resultsPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
}

sourceSets["main"].java.srcDirs("src/main/gen")

intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
        version = properties("pluginVersion")

        description = projectDir.resolve("README.md").readText().lines().run {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
            }
            subList(indexOf(start) + 1, indexOf(end))
        }.joinToString("\n").run { markdownToHTML(this) }

        changeNotes = provider {
            with(changelog) {
                renderItem(
                    getOrNull(properties("pluginVersion")) ?: getLatest(),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
        channels = providers.gradleProperty("pluginVersion").map {
            listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
        }
    }
}

tasks {
    generateLexer {
        sourceFile.set(project.layout.projectDirectory.file("src/main/grammars/Just.flex"))
        targetOutputDir.set(project.layout.projectDirectory.dir("src/main/gen/org/mvnsearch/plugins/just/lang/lexer/"))
        purgeOldFiles.set(true)
    }

    generateParser {
        sourceFile.set(project.layout.projectDirectory.file("src/main/grammars/Just.bnf"))
        targetRootOutputDir.set(project.layout.projectDirectory.dir("src/main/gen"))
        pathToParser.set("/org/mvnsearch/plugins/just/parser/JustParserGenerated.java")
        pathToPsiRoot.set("/org/mvnsearch/plugins/just/lang/psi")
        purgeOldFiles.set(true)
    }

    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }
}
