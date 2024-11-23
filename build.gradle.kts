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

group = "shop.itbug"
version = "2.1.2"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
        releases()
        marketplace()
        jetbrainsRuntime()
    }
}


//RR:
//rustRover("2024.1.5")
//bundledPlugins("JavaScriptBase","com.jetbrains.rust","org.toml.lang")
///        sinceBuild.set("241.17890")
//        untilBuild.set("242.*")

// IDEA
//intellijIdeaUltimate("2024.2")
//plugins("com.jetbrains.rust:242.20224.309")
//bundledPlugins("org.toml.lang","JavaScript")


// rr 2024.2



var isRust = false

dependencies {
    intellijPlatform {

        if(isRust){
            local("/Users/ldd/Applications/RustRover.app")
            bundledPlugins("JavaScript", "com.jetbrains.rust", "org.toml.lang", "com.intellij.modules.json")
        }else{
            local("/Applications/IntelliJ IDEA Ultimate.app")
            plugins("com.jetbrains.rust:243.21565.245")
            bundledPlugins("org.toml.lang","JavaScript","com.intellij.modules.json","com.intellij.database")
        }


        zipSigner()
        instrumentationTools()
        pluginVerifier()
        jetbrainsRuntime()
    }
}

intellijPlatform {
    pluginVerification {
        ides {
            local("/Users/ldd/Applications/RustRover.app")
        }
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
        channels = listOf(ProductRelease.Channel.EAP)
        types = listOf(IntelliJPlatformType.RustRover)
    }


    buildSearchableOptions {
        enabled = false
    }
}

changelog {
    version = project.version as String
    path = file("CHANGELOG.md").canonicalPath
    groups.empty()
}
