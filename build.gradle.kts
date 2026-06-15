plugins {
    id("java")
    id("com.gradleup.shadow") version "9.1.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "com.hibiscusmc"
version = "0.1.0"

val serverVersion = "26.1.2"
val serverSnapshot = "build.+"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.hibiscusmc.com/releases/")
}

dependencies {
    // PaperMC
    paperweight.paperDevBundle("$serverVersion.$serverSnapshot")

    // Inject
    implementation("team.unnamed:inject:2.0.1")
    // Command-Flow
    implementation("team.unnamed:commandflow-bukkit-commandmap:0.7.2") {
        exclude("net.kyori")
    }

    // TriumphGUI
    implementation("dev.triumphteam:triumph-gui:3.1.13") {
        exclude("net.kyori")
        exclude("com.google.gson")
    }

    // HibiscusCommons
    compileOnly("me.lojosho:HibiscusCommons:0.9.1")
    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.12.2")

    // Lombok
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    compileOnly("org.projectlombok:lombok:1.18.42")

    // Configurate
    compileOnly("org.spongepowered:configurate-yaml:4.2.0")
    // HikariCP
    compileOnly("com.zaxxer:HikariCP:7.0.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks {
    shadowJar {
        archiveClassifier.set(fetchCommit())

        val main = "${rootProject.group}.libs"

        relocate("dev.triumphteam.gui", "$main.gui")
        relocate("team.unnamed.inject", "$main.inject")
        relocate("team.unnamed.commandflow", "$main.commandflow")

        archiveFileName.set("HMCClaims-${version}.jar")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        downloadPlugins {
            modrinth("placeholderapi", "2.12.2")
            modrinth("luckperms", "v5.5.17-bukkit")
            url("https://repo.hibiscusmc.com/releases/me/lojosho/HibiscusCommons/0.9.1/HibiscusCommons-0.9.1.jar")
        }

        minecraftVersion(serverVersion)
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005")
    }

    processResources {
        filteringCharset = "UTF-8"

        filesMatching("paper-plugin.yml") {
            expand(
                "project" to project,
                "serverVersion" to serverVersion
            )
        }
    }
}

fun fetchCommit(): String {
    return try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .redirectErrorStream(true)
            .start()

        val hash = process.inputStream
            .bufferedReader().use { it.readLine().trim() }

        if (hash.startsWith("fatal:")) throw Exception()
        else hash
    } catch (_: Exception) {
        ""
    }
}