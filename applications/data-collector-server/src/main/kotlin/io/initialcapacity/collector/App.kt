package io.initialcapacity.collector

import TmdbClient
import io.initialcapacity.model.DataGateway
import io.initialcapacity.queue.MessageQueue
import org.slf4j.Logger
import java.net.URI
import org.slf4j.LoggerFactory

fun main() {
    val rabbitUrl = System.getenv("CLOUDAMQP_URL")?.let(::URI)
            ?: throw RuntimeException("Please set the CLOUDAMQP_URL environment variable")
    val dbUrl = System.getenv("JDBC_DATABASE_URL")
            ?: throw RuntimeException("Please set the JDBC_DATABASE_URL environment variable")
    val tmdbApiKey = System.getenv("TMDB_API_KEY")
            ?: throw java.lang.RuntimeException("Please set the TMDB_API_KEY environment variable")

    val dbConfig = DatabaseConfiguration(dbUrl = dbUrl)

    val collectMoviesQueue = MessageQueue(
            rabbitUrl = rabbitUrl,
            exchangeName = "collect-movies-exchange",
            queueName = "collect-movies-queue"
    )

    val dataGateway = DataGateway(dbConfig.db)

    val tmdbClient = TmdbClient(tmdbApiKey)

    start(collectMoviesQueue, dataGateway, tmdbClient)
}

fun start(
    collectMoviesQueue: MessageQueue,
    gateway: DataGateway,
    tmdbClient: TmdbClient
) {
    val logger = LoggerFactory.getLogger("Collector")

    listenForCollectMoviesRequests(
            collectMoviesQueue,
            worker = CollectMoviesWorker(gateway, tmdbClient),
            logger,
    )
}

fun listenForCollectMoviesRequests(
        collectMoviesQueue: MessageQueue,
        worker: CollectMoviesWorker,
        logger: Logger
) {
    logger.info("listening for collect movies requests")
    collectMoviesQueue.listenToMessages {
        logger.info("received collect movies request")
        worker.execute()
    }
}