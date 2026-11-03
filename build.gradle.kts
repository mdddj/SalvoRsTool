

import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("org.jetbrains.intellij.platform") version "2.19.0"
    id("org.jetbrains.changelog") version "2.2.1"

    // compose
    id("org.jetbrains.compose") version "1.8.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20"
}

group = "shop.itbug"

version = "2.4.1"

repositories {
    mavenCentral()
    google()
    mavenLocal()

    intellijPlatform {
        defaultRepositories()
        releases()
        marketplace()
        jetbrainsRuntime()
    }
}

fun getChangelogVersion(): String {
    val v = project.version as String
    return v
}

dependencies {
    intellijPlatform {
        rustRover("2026.2.5")
        bundledPlugins(
            "JavaScript",
            "com.jetbrains.rust",
            "org.toml.lang",
            "com.intellij.modules.json",
            "com.intellij.database"
        )
        zipSigner()
        pluginVerifier()
        javaCompiler()
        jetbrainsRuntime()
        testFramework(TestFrameworkType.Platform)
        testBundledPlugins("com.jetbrains.rust")

        // jewel
        composeUI()
    }
    testImplementation("junit:junit:4.13.2")
}

var pushToken: String? = System.getenv("PUBLISH_TOKEN")

if (pushToken == null) {
    pushToken = System.getenv("idea_push_token")
}

intellijPlatform {
    pluginVerification {
        ides {
            create(IntelliJPlatformType.RustRover,"2026.2")
        }
    }

    pluginConfiguration {
        productDescriptor {
            code.set("PRUSTX")
            releaseDate.set("20260319")
            releaseVersion.set("24")
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xnon-local-break-continue")
        freeCompilerArgs.add("-Xmulti-dollar-interpolation")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}


tasks {
    withType<KotlinCompile> {
        compilerOptions {
            languageVersion.set(KotlinVersion.KOTLIN_2_3)
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }

    val myChangeLog = provider {
        changelog.renderItem(
            changelog.getOrNull(getChangelogVersion())
                ?: changelog.getUnreleased().withHeader(false).withEmptySections(false),
            Changelog.OutputType.HTML
        )
    }

    val descText = projectDir.resolve("DESCRIPTION.md").readText()

    patchPluginXml {
        sinceBuild.set("262")
        untilBuild.set("262.*")
        changeNotes.set(myChangeLog)
        pluginDescription.set(descText)
    }

    signPlugin {
        var chain = System.getenv("CERTIFICATE_CHAIN").trimIndent()
        var privateKeyString = System.getenv("PRIVATE_KEY").trimIndent()
        if (chain.isEmpty()) {
            chain = file("chain.crt").readText()
        }
        if (privateKeyString.isEmpty()) {
            privateKeyString = file("private.key").readText()
        }
        certificateChain.set(chain)
        privateKey.set(privateKeyString)
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        if (pushToken != null) {
            token.set(pushToken)
        }
    }

    runIde {
        jvmArgs = listOf("-XX:+AllowEnhancedClassRedefinition")
//        args = listOf("/Users/hlx/github/SalvoRsTool/actix/hello-world")
//        args = listOf("/Users/hlx/github/d_blog")
        args = listOf("/Users/ldd/ai-projects/xiaohongshu-helper/src-tauri")
    }

    printProductsReleases {
        channels = listOf(ProductRelease.Channel.RELEASE)
        types = listOf(IntelliJPlatformType.RustRover)
    }

    prepareSandbox {
        doLast {
            val sandboxPathsFile = sandboxConfigDirectory.file("disabled_plugins.txt").get().asFile

            sandboxPathsFile.writeText(
                """
                    org.jetbrains.junie
                    """.trimIndent()
            )
        }
    }

    buildSearchableOptions { enabled = false }
}

idea { module { isDownloadSources = true } }

changelog {
    version = getChangelogVersion()
    path = file("CHANGELOG.md").canonicalPath
    groups.empty()
}
