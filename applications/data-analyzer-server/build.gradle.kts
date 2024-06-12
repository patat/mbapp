plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = "io.initialcapacity.analyzer"

val ktorVersion: String by project
val exposedVersion: String by project
val hikariVersion: String by project
val postgresVersion: String by project
val rabbitVersion: String by project

dependencies {
    implementation(project(":components:data-analyzer"))
    implementation(project(":support:rabbit-support"))

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-freemarker-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")

    implementation("com.zaxxer:HikariCP:$hikariVersion")

    implementation("org.postgresql:postgresql:$postgresVersion")

    implementation("com.rabbitmq:amqp-client:$rabbitVersion")
}

task<JavaExec>("run") {
    classpath = files(tasks.jar)
    environment("JDBC_DATABASE_URL", "jdbc:postgresql://localhost:5432/mb_dev?user=mbapp&password=mbapp_password")
    environment("CLOUDAMQP_URL", "amqp://localhost:5672")
}

tasks {
    jar {
        manifest { attributes("Main-Class" to "io.initialcapacity.analyzer.AppKt") }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from({
            configurations.runtimeClasspath.get()
                    .filter { it.name.endsWith("jar") }
                    .map(::zipTree)
        })
    }
}