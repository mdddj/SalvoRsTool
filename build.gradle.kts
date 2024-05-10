import org.jetbrains.changelog.Changelog

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
    id("org.jetbrains.changelog") version "2.2.0"
}

group = "shop.itbug"
version = "1.5.2"

repositories {
    mavenCentral()
}

intellij {
    version.set("LATEST-EAP-SNAPSHOT")
//    localPath.set("/Users/ldd/Applications/RustRover.app/Contents")
//    localSourcesPath.set("/Users/hlx/github/intellij-community")
    type.set("RR")
    plugins.set(listOf("com.jetbrains.rust","JavaScriptBase"))
}

val pushToken: String? = System.getenv("PUBLISH_TOKEN")

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }



    val myChangeLog =  provider {
        changelog.renderItem(
            changelog
                .getOrNull(project.version as String) ?: changelog.getUnreleased()
                .withHeader(false)
                .withEmptySections(false),
            Changelog.OutputType.HTML
        )
    }

    val descText = projectDir.resolve("DESCRIPTION.md").readText()

    println(descText)

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
        if(pushToken != null) {
            token.set(pushToken)
        }
    }

    runIde {
        jvmArgs = listOf("-XX:+AllowEnhancedClassRedefinition")
    }

}

changelog {
    version = project.version as String
    path = file("CHANGELOG.md").canonicalPath
    groups.empty()
}