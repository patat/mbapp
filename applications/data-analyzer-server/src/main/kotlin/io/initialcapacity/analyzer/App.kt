package io.initialcapacity.analyzer

import io.initialcapacity.collector.DatabaseConfiguration
import io.initialcapacity.model.DataGateway
import io.initialcapacity.queue.MessageQueue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

fun main() {
    runBlocking {
        val rabbitUrl = System.getenv("CLOUDAMQP_URL")?.let(::URI)
                ?: throw RuntimeException("Please set the CLOUDAMQP_URL environment variable")
        val dbUrl = System.getenv("JDBC_DATABASE_URL")
                ?: throw RuntimeException("Please set the JDBC_DATABASE_URL environment variable")

        val dbConfig = DatabaseConfiguration(dbUrl = dbUrl)
        val dataGateway = DataGateway(dbConfig.db)

        val collectMoviesQueue = MessageQueue(
            rabbitUrl = rabbitUrl,
            exchangeName = "collect-movies-exchange",
            queueName = "collect-movies-queue"
        )

        val showcaseMoviesQueue = MessageQueue(
            rabbitUrl = rabbitUrl,
            exchangeName = "showcase-movies-exchange",
            queueName = "showcase-movies-queue"
        )

        val nextRoundQueue = MessageQueue(
            rabbitUrl = rabbitUrl,
            exchangeName = "next-round-exchange",
            queueName = "next-round-queue"
        )

        val showcaseMoviesWorker = ShowcaseMoviesWorker(dataGateway, collectMoviesQueue)
        val nextRoundWorker = NextRoundWorker(dataGateway)

        listenForShowcaseMoviesRequests(
            showcaseMoviesQueue,
            worker = showcaseMoviesWorker,
        )

        listenForNextRoundRequests(
            nextRoundQueue,
            worker = nextRoundWorker,
        )
    }
}

fun listenForShowcaseMoviesRequests(
        showcaseMoviesQueue: MessageQueue,
        worker: ShowcaseMoviesWorker,
) {
    val logger = LoggerFactory.getLogger("ShowcaseMovies")

    logger.info("listening for showcase movies requests")
    showcaseMoviesQueue.listenToMessages {
        logger.info("received showcase movies request")
        val message = Json.decodeFromString<ShowcaseMoviesMessage>(it)
        worker.setBattleMovies(message.battleId)
    }
}

fun listenForNextRoundRequests(
        nextRoundQueue: MessageQueue,
        worker: NextRoundWorker
) {
    val logger = LoggerFactory.getLogger("NextRound")

    logger.info("listening for next round requests")
    nextRoundQueue.listenToMessages {
        logger.info("received next round request")
        val message = Json.decodeFromString<NextRoundMessage>(it)
        worker.setNextRound(message)
    }
}

@Serializable
data class ShowcaseMoviesMessage(
        val battleId: Long,
)