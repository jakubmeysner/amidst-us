version = "1.0.0-SNAPSHOT"

plugins {
  kotlin("jvm") version "1.4.20"
  `java-library`
  kotlin("plugin.serialization") version "1.4.20"
  id("com.github.johnrengelman.shadow") version "6.1.0"
}

dependencies {
  implementation("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
  implementation("net.jitse:npclib-api:2.11.1-SNAPSHOT")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

repositories {
  jcenter()

  maven {
    url = uri("https://hub.spigotmc.org/nexus/content/repositories/public")
  }

  maven {
    url = uri("https://oss.sonatype.org/content/groups/public")
  }
}

tasks {
  build {
    dependsOn(shadowJar)
  }

  jar {
    enabled = false
  }

  shadowJar {
    archiveClassifier.set("")
  }

  processResources {
    expand("version" to version)
  }
}
