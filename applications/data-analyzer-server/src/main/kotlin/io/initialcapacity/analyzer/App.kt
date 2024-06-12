package io.initialcapacity.analyzer

import com.rabbitmq.client.ConnectionFactory
import io.initialcapacity.collector.DatabaseConfiguration
import io.initialcapacity.rabbitsupport.*
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.net.URI

fun Application.module() {
    val logger = LoggerFactory.getLogger(this.javaClass)

    val rabbitUrl = System.getenv("RABBIT_URL")?.let(::URI)
            ?: throw RuntimeException("Please set the RABBIT_URL environment variable")
    val dbUrl = System.getenv("DATABASE_URL")
            ?: throw RuntimeException("Please set the DATABASE_URL environment variable")

    val dbConfig = DatabaseConfiguration(dbUrl = dbUrl)

    val connectionFactory = buildConnectionFactory(rabbitUrl)
    val battlesExchange = RabbitExchange(
            name = "battles-exchange",
            type = "direct",
            routingKeyGenerator = { _: String -> "42" },
            bindingKey = "42",
    )
    val showcaseMoviesQueue = RabbitQueue("showcase-movies")
    connectionFactory.declareAndBind(exchange = battlesExchange, queue = showcaseMoviesQueue)

    val roundsExchange = RabbitExchange(
            name = "rounds-exchange",
            type = "direct",
            routingKeyGenerator = { _: String -> "42" },
            bindingKey = "42",
    )
    val roundsQueue = RabbitQueue("next-round")
    connectionFactory.declareAndBind(exchange = roundsExchange, queue = roundsQueue)

    listenForShowcaseMoviesRequests(
            connectionFactory,
            showcaseMoviesQueue,
            worker = ShowcaseMoviesWorker(dbConfig.db),
            logger,
    )

    listenForNextRoundRequests(
            connectionFactory,
            roundsQueue,
            worker = NextRoundWorker(dbConfig.db),
            logger,
    )
}

fun CoroutineScope.listenForShowcaseMoviesRequests(
        connectionFactory: ConnectionFactory,
        showcaseMoviesQueue: RabbitQueue,
        worker: ShowcaseMoviesWorker,
        logger: Logger
) {
    launch {
        logger.info("listening for showcase movies requests")
        val channel = connectionFactory.newConnection().createChannel()
        listen(queue = showcaseMoviesQueue, channel = channel) {
            logger.debug("received showcase movies request")
            val message = Json.decodeFromString<ShowcaseMoviesMessage>(it)
            worker.setBattleMovies(message.battleId)
        }
    }
}

fun CoroutineScope.listenForNextRoundRequests(
        connectionFactory: ConnectionFactory,
        roundQueue: RabbitQueue,
        worker: NextRoundWorker,
        logger: Logger
) {
    launch {
        logger.info("listening for next round requests")
        val channel = connectionFactory.newConnection().createChannel()
        listen(queue = roundQueue, channel = channel) {
            logger.debug("received next round request")
            val message = Json.decodeFromString<NextRoundMessage>(it)
            worker.setNextRound(message)
        }
    }
}

@Serializable
private data class ShowcaseMoviesMessage(
    val battleId: Long,
)

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt() ?: 8887
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module() }).start(wait = true)
}