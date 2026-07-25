plugins {
    id("fabric-loom") version "1.17.13"
    id("maven-publish")
    id("dev.kikugie.stonecutter")
}

val mod_version = project.property("mod_version") as String
val maven_group = project.property("maven_group") as String
val archives_base_name = project.property("archives_base_name") as String
val minecraft_version = project.property("minecraft_version") as String
val yarn_mappings = project.property("yarn_mappings") as String
val loader_version = project.property("loader_version") as String
val fabric_version = project.property("fabric_version") as String

version = mod_version
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
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", minecraft_version)
    inputs.property("loader_version", loader_version)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to minecraft_version,
            "loader_version" to loader_version
        )
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
