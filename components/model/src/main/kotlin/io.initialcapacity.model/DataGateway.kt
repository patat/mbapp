package io.initialcapacity.model

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

const val BATTLE_MOVIES_COUNT = 16
class DataGateway(private val db: Database) {
    fun getFreshMovies() = transaction(db) {
        val battleCount = MovieBattleRelationTable.battleId.countDistinct().alias("battleCount")
        val leastBattledMovieIds = (MovieTable leftJoin MovieBattleRelationTable)
                .slice(MovieTable.id, battleCount)
                .selectAll()
                .groupBy(MovieTable.id)
                .orderBy(battleCount, SortOrder.ASC_NULLS_FIRST)
                .limit(BATTLE_MOVIES_COUNT)
                .map { it[MovieTable.id] }

        MovieTable.select {
            MovieTable.id inList leastBattledMovieIds
        }.map{
            it.toMovie()
        }
    }

    fun saveMovie(movie: Movie) = transaction(db) {
        val savedMovies = MovieTable.select {MovieTable.tmdbId eq movie.tmdbId}.map { it.toMovie() }

        if (savedMovies.isNotEmpty()) {
            savedMovies.forEach { savedMovie ->
                MovieTable.update ({ MovieTable.id eq savedMovie.id }) {
                    it[tmdbId] = movie.tmdbId
                    it[title] = movie.title
                    it[overview] = movie.overview
                    it[posterPath] = movie.posterPath
                }
            }
        } else {
            MovieTable.insert {
                it[tmdbId] = movie.tmdbId
                it[title] = movie.title
                it[overview] = movie.overview
                it[posterPath] = movie.posterPath
            }
        }
    }

    fun getMovieByTmdbId(tmdbId: Int) = transaction {
        MovieTable
                .select { MovieTable.tmdbId eq tmdbId }
                .limit(1).map {
                    it.toMovie()
                }[0]
    }

    fun createBattle() = transaction(db) {
        val savedBattleId = BattleTable.insert {}
                .getOrNull(BattleTable.id) ?: throw RuntimeException("Couldn't get fresh battle")

        savedBattleId.value
    }

    fun addMoviesToBattle(battleId: Long, movieIds: List<Long>) = transaction(db) {
        movieIds.forEach {movieId ->
            MovieBattleRelationTable.insert {
                it[MovieBattleRelationTable.movieId] = movieId
                it[MovieBattleRelationTable.battleId] = battleId
            }
        }
    }

    fun getBattleMovies(battleId: Long) = transaction(db) {
        (MovieBattleRelationTable innerJoin MovieTable).select {
            MovieBattleRelationTable.battleId eq battleId
        }.limit(BATTLE_MOVIES_COUNT).map {
            it.toMovie()
        }
    }

    fun createRound(battleId: Long) = transaction(db) {
        val savedRoundId = RoundTable.insert { it[this.battleId] = battleId }
                .getOrNull(RoundTable.id) ?: throw RuntimeException("Couldn't create next round")

        savedRoundId.value
    }

    fun getRoundById(roundId: Long) = transaction(db) {
        RoundTable
                .select { RoundTable.id eq roundId }
                .limit(BATTLE_MOVIES_COUNT).map {
                    it.toRound()
                }[0]
    }

    fun updateRound(roundId: Long, round: Round) = transaction(db) {
        val savedRound = RoundTable
                .select { RoundTable.id eq roundId }
                .limit(BATTLE_MOVIES_COUNT).map {
                    it.toRound()
                }[0]

        RoundTable
                .update ({ RoundTable.id eq roundId }) {
                    it[winnerId] = round.winnerId ?: savedRound.winnerId
                    it[movie1Id] = round.movie1Id ?: savedRound.movie1Id
                    it[movie2Id] = round.movie2Id ?: savedRound.movie2Id
                }
    }

    fun getBattleRounds(battleId: Long) = transaction(db) {
        RoundTable
                .select { RoundTable.battleId eq battleId }
                .map {
                    it.toRound()
                }
    }
}

private object MovieBattleRelationTable : Table("movies_battles_relation") {
    val movieId = long("movie_id").references(MovieTable.id)
    val battleId = long("battle_id").references(BattleTable.id)
}