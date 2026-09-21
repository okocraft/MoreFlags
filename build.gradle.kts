plugins {
    alias(libs.plugins.jcommon)
}

jcommon {
    javaVersion = JavaVersion.VERSION_25

    setupPaperRepository()
    setupJUnit(libs.junit.bom)
    setupMockito(libs.mockito)

    commonDependencies {
        compileOnly(libs.platform.paper)
        compileOnly(libs.worldguard.bukkit) {
            exclude("com.google.guava", "guava")
            exclude("com.google.code.gson", "gson")
            exclude("it.unimi.dsi", "fastutil")
        }

        testImplementation(libs.junit.jupiter)
        testImplementation(libs.platform.paper)
        testImplementation(libs.worldguard.bukkit) {
            exclude("com.google.guava", "guava")
            exclude("com.google.code.gson", "gson")
            exclude("it.unimi.dsi", "fastutil")
        }
    }
}

group = "net.okocraft.moreflags"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
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
