import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.intellij.platform") version "2.0.1"
    id("org.jetbrains.changelog") version "2.2.0"
}

group = "shop.itbug"
version = "2.0.5"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
        releases()
        marketplace()
    }
}


dependencies {
    intellijPlatform {
        rustRover("2024.1.5")
        bundledPlugins("JavaScriptBase","com.jetbrains.rust","org.toml.lang")
        zipSigner()
        instrumentationTools()
    }
}

intellijPlatform {
    pluginVerification {
        cliPath.set(file("cli.jar"))
        ides {
            local(file("/Users/ldd/Applications/RustRover.app"))
        }
    }
}


val pushToken: String? = System.getenv("PUBLISH_TOKEN")

tasks {

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<KotlinCompile> {
        compilerOptions {
            languageVersion.set(KotlinVersion.KOTLIN_2_0)
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
        sinceBuild.set("241.17890")
        untilBuild.set("242.*")
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

}

changelog {
    version = project.version as String
    path = file("CHANGELOG.md").canonicalPath
    groups.empty()
}
