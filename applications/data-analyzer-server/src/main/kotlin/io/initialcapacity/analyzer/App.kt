package io.initialcapacity.analyzer

import io.initialcapacity.collector.DatabaseConfiguration
import io.initialcapacity.queue.MessageQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

fun CoroutineScope.listenForShowcaseMoviesRequests(
        showcaseMoviesQueue: MessageQueue,
        worker: ShowcaseMoviesWorker,
        logger: Logger
) {
    launch {
        logger.info("listening for showcase movies requests")
        showcaseMoviesQueue.listenToMessages {
            logger.info("received showcase movies request")
            val message = Json.decodeFromString<ShowcaseMoviesMessage>(it)
            worker.setBattleMovies(message.battleId)
        }
    }
}

fun CoroutineScope.listenForNextRoundRequests(
        nextRoundQueue: MessageQueue,
        worker: NextRoundWorker,
        logger: Logger
) {
    launch {
        logger.info("listening for next round requests")
        nextRoundQueue.listenToMessages {
            logger.info("received next round request")
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
    runBlocking {
        val logger = LoggerFactory.getLogger(this.javaClass)

        val rabbitUrl = System.getenv("CLOUDAMQP_URL")?.let(::URI)
                ?: throw RuntimeException("Please set the CLOUDAMQP_URL environment variable")
        val dbUrl = System.getenv("JDBC_DATABASE_URL")
                ?: throw RuntimeException("Please set the JDBC_DATABASE_URL environment variable")

        val dbConfig = DatabaseConfiguration(dbUrl = dbUrl)

        val showcaseMoviesQueue = MessageQueue(
            rabbitUrl = rabbitUrl,
            exchangeName = "showcase-movies-exchange",
            queueName = "showcase-movies-queue"
        )

        val nextRoundQueue = MessageQueue(
            rabbitUrl = rabbitUrl,
            exchangeName = "next-round-exchange",
            queueName = "next-round-queue"
        );

        listenForShowcaseMoviesRequests(
                showcaseMoviesQueue = showcaseMoviesQueue,
                worker = ShowcaseMoviesWorker(dbConfig.db),
                logger,
        )

        listenForNextRoundRequests(
                nextRoundQueue = nextRoundQueue,
                worker = NextRoundWorker(dbConfig.db),
                logger,
        )
    }
}