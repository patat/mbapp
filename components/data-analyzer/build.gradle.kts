plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

val exposedVersion: String by project
val ktorVersion: String by project

dependencies {
    implementation(project(":components:model"))
    implementation(project(":support:workflow-support"))

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("uk.co.conoregan:themoviedbapi:2.0.4")
}