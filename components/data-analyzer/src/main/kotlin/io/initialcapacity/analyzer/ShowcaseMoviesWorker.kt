package io.initialcapacity.analyzer

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import io.initialcapacity.model.DataGateway
import io.initialcapacity.queue.MessageQueue

class ShowcaseMoviesWorker(
    private val gateway: DataGateway,
    private val collectMoviesQueue: MessageQueue
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun setBattleMovies(battleId: Long) {
            logger.info("starting setting battle movies.")

            val movies = gateway.getFreshMovies()

            if (movies.isNotEmpty()) {
                gateway.addMoviesToBattle(battleId, movies.map { it.id })
            }
        
            collectMoviesQueue.publishMessage("collect movies")

            logger.info("completed to set battle movies.")
    }
}