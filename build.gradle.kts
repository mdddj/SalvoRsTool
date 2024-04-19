plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
    id("org.jetbrains.changelog") version "1.3.1"
}

group = "shop.itbug"
version = "v1.1.1"

repositories {
    mavenCentral()
}

intellij {
    version.set("LATEST-EAP-SNAPSHOT")
    type.set("RR")
    plugins.set(listOf("com.jetbrains.rust"))
}

val pushToken: String? = System.getenv("idea_push_token")

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
        changeNotes.set("""
            <div>
            <h1>1.1.0</h1>
            <p>
            Optimize the presentation of underlined database table names, which will be changed to camel case naming method
            </p>
            </div>
        """.trimIndent())
    }

    signPlugin {
        certificateChainFile.set(file("chain.crt"))
        privateKeyFile.set(file("private.key"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

//    publishPlugin {
//        if(pushToken != null) {
//            token.set(pushToken)
//        }
//
//    }

    runIde {
        jvmArgs = listOf("-XX:+AllowEnhancedClassRedefinition")
    }

}
