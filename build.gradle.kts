import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    java
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.6.0"
    id("org.jetbrains.changelog") version "2.2.0"
}
group = "shop.itbug"
version = "2.2.6"

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
        rustRover("2025.1.3")
        bundledPlugins("JavaScript", "com.jetbrains.rust", "org.toml.lang", "com.intellij.modules.json","com.intellij.database")
        zipSigner()
        pluginVerifier()
        javaCompiler()
        jetbrainsRuntime()
    }
}


var pushToken: String? = System.getenv("PUBLISH_TOKEN")

if(pushToken == null){
    pushToken = System.getenv("idea_push_token")
}

intellijPlatform {
    pluginVerification {
        ides {
            local("/Applications/RustRover.app")
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
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {

    withType<KotlinCompile> {
        compilerOptions {
            languageVersion.set(KotlinVersion.KOTLIN_2_0)
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    val myChangeLog = provider {
        changelog.renderItem(
            changelog
                .getOrNull(getChangelogVersion()) ?: changelog.getUnreleased()
                .withHeader(false)
                .withEmptySections(false),
            Changelog.OutputType.HTML
        )
    }

    val descText = projectDir.resolve("DESCRIPTION.md").readText()



    patchPluginXml {
        sinceBuild.set("251")
        untilBuild.set("251.*")
        changeNotes.set(myChangeLog)
        pluginDescription.set(descText)
    }

    signPlugin {
        var chain = System.getenv("CERTIFICATE_CHAIN").trimIndent()
        var privateKeyString = System.getenv("PRIVATE_KEY").trimIndent()
        if(chain.isEmpty()){
            chain = file("chain.crt").readText()
        }
        if(privateKeyString.isEmpty()) {
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
        autoReload = true
        jvmArgs = listOf("-XX:+AllowEnhancedClassRedefinition")
        args = listOf(
            "/Users/hlx/github/d_blog"
        )
    }

    printProductsReleases {
        channels = listOf(ProductRelease.Channel.RELEASE)
        types = listOf(IntelliJPlatformType.RustRover)
    }

    buildSearchableOptions {
        enabled = false
    }
}

idea {
    module {
        isDownloadSources = true
    }
}



changelog {
    version = getChangelogVersion()
    path = file("CHANGELOG.md").canonicalPath
    groups.empty()
}
