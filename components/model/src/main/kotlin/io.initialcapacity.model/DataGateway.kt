package io.initialcapacity.model

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

val BATTLE_MOVIES_COUNT = 16
class DataGateway(private val db: Database) {

    fun getMovies() = transaction(db) {
        MovieTable
                .select { MovieTable.tmdbId greater 0 }
                .limit(BATTLE_MOVIES_COUNT).map {
                    it.toMovie()
                }
    }

    fun saveMovie(tmdbId: Int, title: String, overview: String, posterPath: String) = transaction(db) {
        MovieTable.insert {
            it[MovieTable.tmdbId] = tmdbId
            it[MovieTable.title] = title
            it[MovieTable.overview] = overview
            it[MovieTable.posterPath] = posterPath
        }
    }
    fun getBattleById(id: Long) = transaction(db) {
        BattleTable
                .select { BattleTable.id eq id }
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
}

private object MovieBattleRelationTable : Table("movies_battles_relation") {
    val movieId = long("movie_id").references(MovieTable.id)
    val battleId = long("battle_id").references(BattleTable.id)
}