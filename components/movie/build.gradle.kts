val ktorVersion: String by project
val exposedVersion: String by project

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
}