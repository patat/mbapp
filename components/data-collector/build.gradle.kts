val exposedVersion: String by project

dependencies {
    implementation(project(":components:movie"))
    implementation(project(":support:workflow-support"))

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("uk.co.conoregan:themoviedbapi:2.0.4")
}