package io.initialcapacity.migrate

import org.flywaydb.core.Flyway
fun main() {
    val flyway = Flyway.configure().dataSource(
            System.getenv("JDBC_DATABASE_URL"),
            System.getenv("JDBC_DATABASE_USERNAME"),
            System.getenv("JDBC_DATABASE_PASSWORD")
    )

    flyway.load().migrate()
}
