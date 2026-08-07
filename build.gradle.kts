plugins {
    id("fabric-loom") version "1.17.13"
    id("maven-publish")
    id("dev.kikugie.stonecutter")
}

val mod_revision = project.property("mod_revision") as String
val maven_group = project.property("maven_group") as String
val archives_base_name = project.property("archives_base_name") as String
val minecraft_version = project.property("minecraft_version") as String
val yarn_mappings = project.property("yarn_mappings") as String
val loader_version = project.property("loader_version") as String
val fabric_version = project.property("fabric_version") as String

version = "$minecraft_version-$mod_revision"
group = maven_group

base {
    archivesName.set(archives_base_name)
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("ddv_fishing") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }
}

fabricApi {
    configureDataGeneration {
        client.set(true)
    }
}

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://api.modrinth.com/maven")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    val gVersion = project.findProperty("geckolib_version") as? String
    val gGroup = project.findProperty("geckolib_group") as? String ?: "software.bernie.geckolib"
    val v = gVersion ?: when (minecraft_version) {
        "1.21.1" -> "4.6.6"
        "1.21.2", "1.21.9", "1.21.10" -> "5.3-alpha-3"
        "1.21.3" -> "4.7.1"
        "1.21.4" -> "4.8.5"
        "1.21.5" -> "5.1.0"
        "1.21.6" -> "5.2.0"
        "1.21.7" -> "5.2.1"
        "1.21.8" -> "5.2.2"
        "1.21.11" -> "5.4.4"
        else -> null
    }

    val borrowsForeignBuild = minecraft_version in setOf("1.21.2", "1.21.9")

    if (v != null) {
        if (gGroup == "maven.modrinth" || (borrowsForeignBuild && gGroup == "software.bernie.geckolib")) {
            modImplementation("maven.modrinth:geckolib:$v")
        } else {
            modImplementation("$gGroup:geckolib-fabric-$minecraft_version:$v")
        }
    }
}

val (resourcePackFormat, dataPackFormat) = when (minecraft_version) {
    "1.21.1" -> 34 to 48
    "1.21.2", "1.21.3" -> 42 to 57
    "1.21.4" -> 46 to 61
    "1.21.5" -> 55 to 71
    "1.21.6" -> 63 to 80
    "1.21.7", "1.21.8" -> 64 to 81
    "1.21.9", "1.21.10" -> 69 to 88
    "1.21.11" -> 75 to 94
    else -> throw GradleException("No pack format mapping for Minecraft $minecraft_version")
}

val usesLegacyGeckoLibAssets = minecraft_version in setOf("1.21.1", "1.21.3", "1.21.4")

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", minecraft_version)
    inputs.property("loader_version", loader_version)
    inputs.property("resource_pack_format", resourcePackFormat)
    inputs.property("data_pack_format", dataPackFormat)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to minecraft_version,
            "loader_version" to loader_version
        )
    }

    filesMatching("pack.mcmeta") {
        expand(
            "resource_pack_format" to resourcePackFormat,
            "data_pack_format" to dataPackFormat
        )
    }

    if (usesLegacyGeckoLibAssets) {
        sourceSets.main.get().resources.srcDirs.forEach { srcDir ->
            from(File(srcDir, "assets/ddv_fishing/geckolib/animations")) {
                into("assets/ddv_fishing/animations")
            }
            from(File(srcDir, "assets/ddv_fishing/geckolib/models")) {
                into("assets/ddv_fishing/geo")
            }
        }
    }
}

val targetJavaVersion = 21
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_$archives_base_name" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = archives_base_name
            from(components["java"])
        }
    }
    repositories {
    }
}

tasks.register("printSourceSets") {
    doLast {
        sourceSets.forEach { srcSet ->
            println("[" + srcSet.name + "]")
            srcSet.allJava.srcDirs.forEach { println("  " + it) }
        }
    }
}

tasks.register("printClasspath") {
    doLast {
        configurations.named("compileClasspath").get().files.forEach { println(it) }
    }
}