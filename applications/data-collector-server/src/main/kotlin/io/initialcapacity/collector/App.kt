package io.initialcapacity.collector

import com.rabbitmq.client.ConnectionFactory
import io.initialcapacity.rabbitsupport.*
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.*
import io.ktor.server.routing.get
import io.ktor.server.routing.Routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import java.util.*
import java.net.URI
import org.slf4j.LoggerFactory

fun Application.module() {
    val logger = LoggerFactory.getLogger(this.javaClass)

    val rabbitUrl = System.getenv("RABBIT_URL")?.let(::URI)
        ?: throw RuntimeException("Please set the RABBIT_URL environment variable")
    val dbUrl = System.getenv("DATABASE_URL")
        ?: throw RuntimeException("Please set the DATABASE_URL environment variable")

    val dbConfig = DatabaseConfiguration(dbUrl = dbUrl)

    val connectionFactory = buildConnectionFactory(rabbitUrl)
    val collectMoviesExchange = RabbitExchange(
            name = "collect-movies-exchange",
            type = "direct",
            routingKeyGenerator = { _: String -> "42" },
            bindingKey = "42",
    )
    val collectMoviesQueue = RabbitQueue("collect-movies")
    connectionFactory.declareAndBind(exchange = collectMoviesExchange, queue = collectMoviesQueue)

    listenForCollectMoviesRequests(
            connectionFactory,
            collectMoviesExchange,
            collectMoviesQueue,
            worker = CollectMoviesWorker(dbConfig.db),
            logger,
    )

    install(Routing) {
        get("/") {
            call.respondText("hello!", ContentType.Text.Html)
        }

        get("/collect-movies") {
            val publishCollectMovies = publish(connectionFactory, collectMoviesExchange)

            logger.debug("publishing collect movies")
            publishCollectMovies("collect movies")

            call.respondText("collect movies published!", ContentType.Text.Html)
        }
    }
}

fun CoroutineScope.listenForCollectMoviesRequests(
        connectionFactory: ConnectionFactory,
        collectMoviesExchange: RabbitExchange,
        collectMoviesQueue: RabbitQueue,
        worker: CollectMoviesWorker,
        logger: Logger
) {
    launch {
        logger.info("listening for collect movies requests")
        val channel = connectionFactory.newConnection().createChannel()
        listen(queue = collectMoviesQueue, channel = channel) {
            logger.debug("received collect movies request")
            worker.execute()
        }
    }
}

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt() ?: 8886
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module() }).start(wait = true)
}