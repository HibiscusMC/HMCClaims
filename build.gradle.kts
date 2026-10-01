plugins {
    id("java")
    id("maven-publish")
    alias(libs.plugins.lombok)
    alias(libs.plugins.shadow)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.paperweight)
}

group = "com.hibiscusmc.hmcclaims"
version = "0.6.0"

val serverVersion = libs.versions.minecraft.get()
val serverSnapshot = "build.+"

repositories {
    maven("https://repo.hibiscusmc.com/releases/")
    maven("https://repo.krzu.me/releases/")

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://repo.opencollab.dev/main/")
    maven("https://jitpack.io")

    mavenCentral()
}

dependencies {
    // PaperMC
    paperweight.paperDevBundle("$serverVersion.$serverSnapshot")

    // Protobuf
    implementation(libs.protobuf.java)

    // Inject
    implementation(libs.inject)
    // Command-Flow
    implementation(libs.commandflow.bukkit.commandmap) {
        exclude("net.kyori")
    }

    // InvUI
    implementation(libs.invui)

    // HibiscusCommons
    compileOnly(libs.hibiscus.commons)
    // PlaceholderAPI
    compileOnly(libs.placeholderapi)
    // Floodgate (Bedrock forms via Geyser)
    compileOnly(libs.floodgate.api)
    // Vault (claim block purchases)
    compileOnly(libs.vault.api) {
        exclude("org.bukkit")
    }

    // hx-config
    implementation(libs.hxconfig)

    // HikariCP
    compileOnly(libs.hikaricp)
}

lombok {
    version = libs.versions.lombok.asProvider()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25

    withSourcesJar()
    withJavadocJar()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protoc.get()}"
    }

    generateProtoTasks {
        all().forEach {
            it.builtins {
                java {}
            }
        }
    }
}

tasks.named("extractIncludeProto") { enabled = false }
tasks.named("extractProto") { enabled = false }

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveBaseName.set(project.name)
        archiveVersion.set(version(project.version.toString()))
        archiveClassifier.set("")

        val main = "${rootProject.group}.libs"

        relocate("xyz.xenondevs.invui", "$main.invui")
        relocate("team.unnamed.inject", "$main.inject")
        relocate("com.google.protobuf", "$main.protobuf")
        relocate("team.unnamed.commandflow", "$main.commandflow")
        relocate("team.hypox.config", "$main.config")

        exclude("colors.bin")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        downloadPlugins {
            modrinth("placeholderapi", libs.versions.papi.modrinth.get())
            modrinth("luckperms", libs.versions.luckperms.download.get())
            url("https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot")
            url("https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot")
            url("https://repo.hibiscusmc.com/releases/me/lojosho/HibiscusCommons/${libs.versions.hibiscus.commons.get()}/HibiscusCommons-${libs.versions.hibiscus.commons.get()}.jar")
            url("https://github.com/MilkBowl/Vault/releases/download/${libs.versions.vault.download.get()}/Vault.jar")
        }

        minecraftVersion(serverVersion)
        jvmArgs(
            "-XX:+AllowEnhancedClassRedefinition",
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
        )
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

    javadoc {
        options {
            encoding(Charsets.UTF_8.name())
            charset(Charsets.UTF_8.name())

            (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "HibiscusMCRepository"
            url = uri("https://repo.hibiscusmc.com/" + fetchVersionType().repo)
            credentials {
                username = System.getenv("HMC_REPOSILITE_USER")
                password = System.getenv("HMC_REPOSILITE_SECRET")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = project.name
            version = version(rootProject.version.toString())

            artifact(tasks.shadowJar)
            artifact(tasks.named("javadocJar"))
            artifact(tasks.named("sourcesJar"))
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

fun version(ver: String): String {
    return ver + if (fetchVersionType() == VersionType.DEVELOPMENT) {
        ".dev." + fetchCommit()
    } else ""
}

fun fetchVersionType(): VersionType {
    return if ((System.getenv("RELEASE") ?: "").isNotEmpty()) VersionType.RELEASE
    else VersionType.DEVELOPMENT
}

enum class VersionType(val repo: String) {
    RELEASE("releases"),
    DEVELOPMENT("snapshots")
}