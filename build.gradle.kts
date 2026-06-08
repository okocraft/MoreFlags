plugins {
    java
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

group = "net.okocraft.moreflags"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.67-stable")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.17") {
        exclude("com.google.guava", "guava")
        exclude("com.google.code.gson", "gson")
        exclude("it.unimi.dsi", "fastutil")
    }
}

tasks.compileJava {
    options.release.set(25)
}

tasks.processResources {
    filesMatching(listOf("plugin.yml")) {
        expand("projectVersion" to version)
    }
}

tasks.jar {
    manifest {
        attributes("paperweight-mappings-namespace" to "mojang")
    }
}
