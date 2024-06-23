plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false
}

repositories {
    mavenCentral()
}

subprojects {
    if (listOf("applications", "components", "support", "databases").contains(name)) return@subprojects

    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.7")
        implementation("org.slf4j:slf4j-simple:2.0.7")

        testImplementation(kotlin("test-junit"))
    }
}

tasks.create("stage") {
    dependsOn("assemble")
}