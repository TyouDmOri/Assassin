/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Author: TyouDm
 */

import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    java
    // paperweight and shadow versions must be string literals in the plugins block
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("com.gradleup.shadow")            version "8.3.8"
    id("me.champeau.jmh")                version "0.7.3"
}

group   = providers.gradleProperty("project.group").get()
version = providers.gradleProperty("project.version").get()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(
            providers.gradleProperty("java.version").get().toInt()
        )
    }
}

// ─── Repositories ─────────────────────────────────────────────────────────────
repositories {
    mavenCentral()

    // Paper dev bundle
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }

    // PacketEvents
    maven("https://repo.codemc.io/repository/maven-releases/") {
        name = "codemc-releases"
    }
    maven("https://repo.codemc.io/repository/maven-snapshots/") {
        name = "codemc-snapshots"
    }
}

// ─── Dependencies ─────────────────────────────────────────────────────────────
dependencies {
    // Paper 1.21.11 dev bundle (provides Paper API + NMS via paperweight)
    paperweight.paperDevBundle(providers.gradleProperty("paper.devBundle").get())

    // PacketEvents — external plugin dependency (NOT shaded)
    compileOnly("com.github.retrooper:packetevents-spigot:${property("packetevents.version")}")

    // HikariCP — connection pool for SQLite/MySQL/MariaDB
    implementation("com.zaxxer:HikariCP:${property("hikari.version")}")

    // Caffeine — in-memory cache (GUI prefs, macro rolling hash, etc.)
    implementation("com.github.ben-manes.caffeine:caffeine:${property("caffeine.version")}")

    // JMH benchmarks (jmh sourceSet only)
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

// ─── Compile options ──────────────────────────────────────────────────────────
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release  = providers.gradleProperty("java.version").get().toInt()
    // Preserve parameter names for Brigadier reflection
    options.compilerArgs.add("-parameters")
}

// ─── Process resources ────────────────────────────────────────────────────────
tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "author"  to "TyouDm"
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

// ─── Shadow JAR ───────────────────────────────────────────────────────────────
tasks.shadowJar {
    archiveClassifier = ""

    // PacketEvents is an external plugin dependency — NOT shaded.
    // Only relocate HikariCP and Caffeine to avoid classpath conflicts.
    relocate("com.zaxxer.hikari",            "dev.tyoudm.assasin.libs.hikari")
    relocate("com.github.benmanes.caffeine", "dev.tyoudm.assasin.libs.caffeine")

    // Exclude unnecessary files from the fat JAR
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("module-info.class")
}

// ─── JMH configuration ────────────────────────────────────────────────────────
jmh {
    // Run with: ./gradlew jmh
    warmupIterations = 3
    iterations       = 5
    fork             = 1
    timeUnit         = "ms"
    benchmarkMode    = listOf("avgt")   // average time
    resultFormat     = "JSON"
    resultsFile      = project.file("${project.buildDir}/reports/jmh/results.json")
}

// ─── Mappings configuration ───────────────────────────────────────────────────
// Paper 1.20.5+ uses Mojang-mapped runtime — no reobfuscation needed
paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

// ─── Build task wiring ────────────────────────────────────────────────────────
tasks.assemble {
    dependsOn(tasks.shadowJar)
}
