package io.initialcapacity.analyzer

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import io.initialcapacity.model.DataGateway
import org.jetbrains.exposed.sql.Database
import kotlinx.serialization.Serializable

class NextRoundWorker(private val db: Database) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun setNextRound(message: NextRoundMessage) {
        runBlocking {
            logger.info("starting setting next round.")

            val gateway = DataGateway(db)

//            val movies = gateway.getMovies()
//            gateway.addMoviesToBattle(battleId, movies.map { it.id })

            logger.info("completed to set next round.")
        }
    }
}

@Serializable
data class NextRoundMessage(
        val battleId: Long,
        val nextRoundId: Long,
        val prevRoundId: Long?,
        val winnerId: Long?,
)