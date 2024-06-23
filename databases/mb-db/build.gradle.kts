plugins {
    id("org.flywaydb.flyway") version "8.5.7"
}

val flywayMigration by configurations.creating
val postgresVersion: String by project

task<JavaExec>("run") {
    classpath = files(tasks.jar)
    environment("JDBC_DATABASE_URL", "jdbc:postgresql://localhost:5432/mb_dev?user=mbapp&password=mbapp_password")
}

dependencies {
    implementation("org.flywaydb:flyway-core:8.5.7")
    implementation("org.postgresql:postgresql:$postgresVersion")

    flywayMigration("org.postgresql:postgresql:$postgresVersion")
}

flyway {
    configurations = arrayOf("flywayMigration")
}

tasks.register<org.flywaydb.gradle.task.FlywayMigrateTask>("devMigrate") {
    url = "jdbc:postgresql://localhost:5432/mb_dev?user=mbapp&password=mbapp_password"
}

tasks.register<org.flywaydb.gradle.task.FlywayCleanTask>("devClean") {
    url = "jdbc:postgresql://localhost:5432/mb_dev?user=mbapp&password=mbapp_password"
}

tasks.register<org.flywaydb.gradle.task.FlywayMigrateTask>("testMigrate") {
    url = "jdbc:postgresql://localhost:5432/mb_test?user=mbapp&password=mbapp_password"
}

tasks.register<org.flywaydb.gradle.task.FlywayCleanTask>("testClean") {
    url = "jdbc:postgresql://localhost:5432/mb_test?user=mbapp&password=mbapp_password"
}

tasks.register<org.flywaydb.gradle.task.FlywayMigrateTask>("prodMigrate") {
    url = System.getenv("JDBC_DATABASE_URL")
}

tasks {
    jar {
        manifest { attributes("Main-Class" to "io.initialcapacity.migrate.FlywayMigrateKt") }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from({
            configurations.runtimeClasspath.get()
                    .filter { it.name.endsWith("jar") }
                    .map(::zipTree)
        })
    }
}
