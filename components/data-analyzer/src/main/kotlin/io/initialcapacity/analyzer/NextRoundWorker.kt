package io.initialcapacity.analyzer

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import io.initialcapacity.model.DataGateway
import io.initialcapacity.model.Movie
import io.initialcapacity.model.Round
import org.jetbrains.exposed.sql.Database
import kotlinx.serialization.Serializable

class NextRoundWorker(private val gateway: DataGateway) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun setNextRound(message: NextRoundMessage) {
        runBlocking {
            logger.info("starting setting next round.")

            if (message.prevRoundId != null && message.winnerId != null) {
                // update winner on previous round
                gateway.updateRound(message.prevRoundId, Round(
                        battleId = message.battleId,
                        winnerId = message.winnerId
                    )
                )
            }

            val battleMovies = gateway.getBattleMovies(message.battleId)
            val battleRounds = gateway.getBattleRounds(message.battleId)

            val nextRound = calculateNextRound(battleId = message.battleId, battleMovies, battleRounds)
            gateway.updateRound(message.nextRoundId, nextRound)

            logger.info("completed to set next round.")
        }
    }
}

fun calculateNextRound(battleId: Long, battleMovies: List<Movie>, battleRounds: List<Round>): Round {
    // filter losers
    val loserMovieIds = battleRounds.filter { it.winnerId !== null }.mapNotNull<Round, Long> {
        if (it.movie1Id == it.winnerId) {
            return@mapNotNull it.movie2Id
        }

        return@mapNotNull it.movie1Id
    }
    val remainingMovies = battleMovies.filter { !loserMovieIds.contains(it.id) }
    // if 1 remaining – it's a winner
    if (remainingMovies.size == 1) {
        val winner = remainingMovies[0]
        return Round(
            battleId = battleId,
            winnerId = winner.id
        )
    }
    // calc stage - min # of wins in movies, calc max # of wins
    val numOfWinsMovies = mutableMapOf<Long, Int>()
    remainingMovies.forEach {movie ->
        val numOfWins = battleRounds.sumOf {round ->
            var score = 0

            if (round.winnerId == movie.id) {
                score = 1
            }

            score
        }

        numOfWinsMovies[movie.id] = numOfWins
    }
    val minOfWins = numOfWinsMovies.minOf { it.value }
    val maxOfWins = numOfWinsMovies.maxOf { it.value }
    // if min == max – next stage, take all
    if (minOfWins == maxOfWins) {
        return Round(
            battleId = battleId,
            movie1Id = remainingMovies[0].id,
            movie2Id = remainingMovies[1].id
        )
    }

    // else calc unbattled movies on stage (take those with min # of wins)
    val unbattledMovies = remainingMovies.filter {
        numOfWinsMovies[it.id] == minOfWins
    }

    if (unbattledMovies.size < 2) {
        throw RuntimeException("cant have less than 2 unbattled movies if no winner")
    }

    return Round(
        battleId = battleId,
        movie1Id = unbattledMovies[0].id,
        movie2Id = unbattledMovies[1].id
    )
}

@Serializable
data class NextRoundMessage(
        val battleId: Long,
        val nextRoundId: Long,
        val prevRoundId: Long?,
        val winnerId: Long?,
)