plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = "io.initialcapacity.collector"

val ktorVersion: String by project
val exposedVersion: String by project
val hikariVersion: String by project
val postgresVersion: String by project
val rabbitVersion: String by project
val mockkVersion: String by project

dependencies {
    implementation(project(":components:data-collector"))
    implementation(project(":components:queue"))

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-freemarker-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.rabbitmq:amqp-client:$rabbitVersion")
    implementation(project(mapOf("path" to ":components:model")))

    testImplementation("io.mockk:mockk:$mockkVersion")
}

task<JavaExec>("run") {
    classpath = files(tasks.jar)
    environment("JDBC_DATABASE_URL", "jdbc:postgresql://localhost:5432/mb_dev?user=mbapp&password=mbapp_password")
    environment("CLOUDAMQP_URL", "amqp://localhost:5672")
}

tasks.create("stage") {
    dependsOn("assemble")
}

tasks {
    jar {
        manifest { attributes("Main-Class" to "io.initialcapacity.collector.AppKt") }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
                .map(::zipTree)
        })
    }
}