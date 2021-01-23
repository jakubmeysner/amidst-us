import org.gradle.language.jvm.tasks.ProcessResources
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

version = "1.0.0-SNAPSHOT"

plugins {
  id("org.jetbrains.kotlin.jvm") version "1.4.20"
  `java-library`
  id("com.github.johnrengelman.shadow") version "6.1.0"
}

repositories {
  jcenter()
  maven {
    url = uri("https://hub.spigotmc.org/nexus/content/repositories/public")
  }
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
  implementation("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
}

tasks {
  build {
    dependsOn(shadowJar)
  }
}

tasks.processResources {
  expand("version" to version)
}

tasks.withType<ShadowJar> {
  classifier = ""
}
