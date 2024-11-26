import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.1.0"
    id("org.jetbrains.changelog") version "2.2.0"
}
var isRust = true
val suf = if (isRust) "RR" else "IU"
group = "shop.itbug"
version = "2.2.0.$suf"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
        releases()
        marketplace()
        jetbrainsRuntime()
    }
}

fun getChangelogVersion(): String {
    val v = project.version as String
    return v.removeSuffix(".$suf")
}


dependencies {
    intellijPlatform {

        if (isRust) {
            rustRover("2024.3")
            bundledPlugins("JavaScript", "com.jetbrains.rust", "org.toml.lang", "com.intellij.modules.json")
            plugins("com.intellij.database:243.15521.0")
        } else {
            local("/Applications/IntelliJ IDEA Ultimate.app")
            plugins("com.jetbrains.rust:243.21565.245")
            bundledPlugins("org.toml.lang", "JavaScript", "com.intellij.modules.json", "com.intellij.database")
        }
        zipSigner()
        instrumentationTools()
        pluginVerifier()
        jetbrainsRuntime()
    }
}


val pushToken: String? = System.getenv("PUBLISH_TOKEN")

tasks {

    withType<KotlinCompile> {
        compilerOptions {
            languageVersion.set(KotlinVersion.KOTLIN_2_1)
        }
    }

    val myChangeLog = provider {
        changelog.renderItem(
            changelog
                .getOrNull(project.version as String) ?: changelog.getUnreleased()
                .withHeader(false)
                .withEmptySections(false),
            Changelog.OutputType.HTML
        )
    }

    val descText = projectDir.resolve("DESCRIPTION.md").readText()


    patchPluginXml {
        sinceBuild.set("243")
        untilBuild.set("243.*")
        changeNotes.set(myChangeLog)
        pluginDescription.set(descText)
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        if (pushToken != null) {
            token.set(pushToken)
        }
    }

    runIde {
        autoReload = true
        jvmArgs = listOf("-XX:+AllowEnhancedClassRedefinition")
    }

    verifyPlugin {
        failureLevel = VerifyPluginTask.FailureLevel.ALL
    }

    printProductsReleases {
        channels = listOf(ProductRelease.Channel.RELEASE)
        types = listOf(IntelliJPlatformType.RustRover)
    }


    buildSearchableOptions {
        enabled = false
    }
}

println(getChangelogVersion())
changelog {
    version = getChangelogVersion()
    path = file("CHANGELOG.md").canonicalPath
    groups.empty()
}
