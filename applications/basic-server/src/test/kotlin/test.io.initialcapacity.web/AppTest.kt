package test.io.initialcapacity.web

import io.initialcapacity.web.DatabaseConfiguration
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import java.net.URI

class AppTest {
    private val rabbitUri = URI("amqp://localhost:5672")
    private val dbConfig =
            DatabaseConfiguration("jdbc:postgresql://localhost:5555/notification_test?user=emailverifier&password=emailverifier")

    @Before
    fun setUp() {
        transaction(dbConfig.db) {
            exec("delete from notifications")
        }
    }
}
