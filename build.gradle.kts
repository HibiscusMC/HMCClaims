plugins {
    id("java")
    id("maven-publish")
    id("io.freefair.lombok") version "9.2.0"
    id("com.gradleup.shadow") version "9.1.0"
    id("com.google.protobuf") version "0.10.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "com.hibiscusmc.hmcclaims"
version = "0.2.3"

val serverVersion = "26.2"
val serverSnapshot = "build.+"

repositories {
    maven("https://repo.hibiscusmc.com/releases/")

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.xenondevs.xyz/releases")

    mavenCentral()
}

dependencies {
    // PaperMC
    paperweight.paperDevBundle("$serverVersion.$serverSnapshot")

    // Protobuf
    implementation("com.google.protobuf:protobuf-java:4.35.1")

    // Inject
    implementation("team.unnamed:inject:2.0.1")
    // Command-Flow
    implementation("team.unnamed:commandflow-bukkit-commandmap:0.8.0") {
        exclude("net.kyori")
    }

    // InvUI
    implementation("xyz.xenondevs.invui:invui:2.3.0")

    // HibiscusCommons
    compileOnly("me.lojosho:HibiscusCommons:0.9.3")
    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.12.3")

    // Configurate
    implementation("org.spongepowered:configurate-core:4.4.5-HMC")
    implementation("org.spongepowered:configurate-yaml:4.4.5-HMC")

    // HikariCP
    compileOnly("com.zaxxer:HikariCP:7.0.2")
}

lombok {
    version = "1.18.42"
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
        artifact = "com.google.protobuf:protoc:4.35.1"
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
        relocate("org.spongepowered.configurate", "$main.configurate")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        downloadPlugins {
            modrinth("placeholderapi", "pIvQcXW8")
            modrinth("luckperms", "v5.5.17-bukkit")
            url("https://repo.hibiscusmc.com/releases/me/lojosho/HibiscusCommons/0.9.3/HibiscusCommons-0.9.3.jar")
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