package io.initialcapacity.collector

import TmdbClient
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import io.initialcapacity.model.DataGateway

class CollectMoviesWorker(private val gateway: DataGateway, private val tmdbClient: TmdbClient) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun execute() {
        runBlocking {
            logger.info("starting data collection.")

            val tmdbMovies = tmdbClient.getMovies()

            tmdbMovies.forEach {
                gateway.saveMovie(it)
            }

            logger.info("completed data collection.")
        }
    }
}