import groovy.lang.Closure

plugins {
    java

    alias(catalog.plugins.kotlin.jvm)
    alias(catalog.plugins.kotlin.plugin.serialization)

    alias(catalog.plugins.git.version)

    alias(catalog.plugins.shadow)

    alias(catalog.plugins.unmined)
}

group = "settingdust.mongo_runtime"

val gitVersion: Closure<String> by extra

version = gitVersion()

val id: String by rootProject.properties
val name: String by rootProject.properties
val author: String by rootProject.properties
val description: String by rootProject.properties

kotlin { jvmToolchain(8) }

base { archivesName = id }

allprojects {
    apply(plugin = "java")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        withSourcesJar()
    }

    repositories {
        mavenCentral()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    base { archivesName.set("${rootProject.base.archivesName.get()}${project.path.replace(":", "-")}") }
}

repositories {
    unimined.modrinthMaven()
    unimined.curseMaven()
    maven("https://maven.cleanroommc.com") {
        content {
            includeGroup("io.github.chaosunity.forgelin")
            includeGroup("zone.rong")
        }
    }
}


unimined.minecraft {
    version("1.12.2")
    mappings {
        searge()
        mcp("stable", "39-1.12")
    }

    if (sourceSet == sourceSets.main.get()) {
        minecraftForge {
            loader("14.23.5.2860")
        }
        runs {
            all {
                jvmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true", "-Dmixin.dumpTargetOnFailure=true")
            }
        }
    }
}

dependencies {
    implementation(catalog.kotlinx.serialization.core)

    catalog.mongodb.driver.let {
        implementation(it)
        shadow(it)
    }
    catalog.kotlinx.bson.let {
        implementation(it)
        shadow(it)
    }

    implementation(catalog.kotlin.forge)
}

val metadata =
    mapOf(
        "group" to group,
        "author" to author,
        "id" to id,
        "name" to name,
        "version" to version,
        "description" to description
    )

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        mergeServiceFiles()

        duplicatesStrategy = DuplicatesStrategy.WARN

        dependencies {
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:"))
            exclude(dependency("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:"))
            exclude(dependency("org.jetbrains.kotlin::"))
            exclude(dependency("org.jetbrains::"))
        }
    }

    withType<ProcessResources> {
        inputs.properties(metadata)
        filesMatching(listOf("mcmod.info")) { expand(metadata) }
    }

    build {
        dependsOn(shadowJar)
    }
}