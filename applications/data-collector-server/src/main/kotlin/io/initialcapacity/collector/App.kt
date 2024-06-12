package io.initialcapacity.collector

import com.rabbitmq.client.ConnectionFactory
import io.initialcapacity.rabbitsupport.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import java.net.URI
import org.slf4j.LoggerFactory

fun CoroutineScope.listenForCollectMoviesRequests(
        connectionFactory: ConnectionFactory,
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
    runBlocking {
        val logger = LoggerFactory.getLogger(this.javaClass)

        val rabbitUrl = System.getenv("CLOUDAMQP_URL")?.let(::URI)
                ?: throw RuntimeException("Please set the CLOUDAMQP_URL environment variable")
        val dbUrl = System.getenv("JDBC_DATABASE_URL")
                ?: throw RuntimeException("Please set the JDBC_DATABASE_URL environment variable")

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
            collectMoviesQueue,
            worker = CollectMoviesWorker(dbConfig.db),
            logger,
        )
    }
}