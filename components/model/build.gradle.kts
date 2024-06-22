plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

val ktorVersion: String by project
val exposedVersion: String by project
val postgresVersion: String by project

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    testImplementation("org.postgresql:postgresql:$postgresVersion")
}

tasks.test {
    exclude("**/*")
}