package io.initialcapacity.collector

import io.initialcapacity.queue.MessageQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import java.net.URI
import org.slf4j.LoggerFactory

fun CoroutineScope.listenForCollectMoviesRequests(
        collectMoviesQueue: MessageQueue,
        worker: CollectMoviesWorker,
        logger: Logger
) {
    launch {
        logger.info("listening for collect movies requests")
        collectMoviesQueue.listenToMessages {
            logger.info("received collect movies request")
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

        val collectMoviesQueue = MessageQueue(
                rabbitUrl = rabbitUrl,
                exchangeName = "collect-movies-exchange",
                queueName = "collect-movies-queue"
        )

        listenForCollectMoviesRequests(
            collectMoviesQueue,
            worker = CollectMoviesWorker(dbConfig.db),
            logger,
        )
    }
}