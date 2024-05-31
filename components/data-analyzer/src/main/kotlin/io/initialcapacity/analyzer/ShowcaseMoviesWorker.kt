package io.initialcapacity.analyzer

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import io.initialcapacity.model.DataGateway
import org.jetbrains.exposed.sql.Database

class ShowcaseMoviesWorker(private val db: Database) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun setBattleMovies(battleId: Long) {
        runBlocking {
            logger.info("starting setting battle movies.")

            val gateway = DataGateway(db)

            val movies = gateway.getMovies()
            gateway.addMoviesToBattle(battleId, movies.map { it.id })

            logger.info("completed to set battle movies.")
        }
    }
}