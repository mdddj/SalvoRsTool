import org.jetbrains.changelog.Changelog
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.intellij.platform") version "2.0.0-beta7"
    id("org.jetbrains.changelog") version "2.2.0"
}

group = "shop.itbug"
version = "1.8.0"

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
        rustRover("2024.1.2")
        bundledPlugins("com.jetbrains.rust","JavaScriptBase")
        zipSigner()
        instrumentationTools()
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
        sinceBuild.set("232")
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
        jvmArgs = listOf("-XX:+AllowEnhancedClassRedefinition")
    }

    test {
        useJUnitPlatform()
    }

    prepareSandbox {
        doNotTrackState("---")
    }

}

changelog {
    version = project.version as String
    path = file("CHANGELOG.md").canonicalPath
    groups.empty()
}

dependencies {
    implementation ("com.alibaba.fastjson2:fastjson2:2.0.51")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
    implementation("com.google.guava:guava:31.1-jre")
    testImplementation("junit:junit:4.13.2")
}
