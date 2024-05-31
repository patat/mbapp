package io.initialcapacity.awaiter

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import io.initialcapacity.model.DataGateway
import io.initialcapacity.model.Movie
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.Database

class ResultsAwaiter(private val db: Database) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun createBattle(): Long {
        return runBlocking {
            logger.info("starting to create battle.")

            val gateway = DataGateway(db)

            val battleID = gateway.createBattle()

            logger.info("completed to create battle.")

            return@runBlocking battleID
        }
    }

    suspend fun waitForBattleMovies(battleId: Long): List<Movie> {
        val moviesDeferred = CompletableDeferred<List<Movie>>()
        val gateway = DataGateway(db)

        repeat(5) {
            delay(200)

            val movies = gateway.getBattleMovies(battleId)

            if (movies.isNotEmpty()) {
                moviesDeferred.complete(movies)
                return@repeat
            }
        }

        return moviesDeferred.await()
    }
}