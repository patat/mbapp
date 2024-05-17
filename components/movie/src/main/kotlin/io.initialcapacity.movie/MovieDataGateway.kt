package io.initialcapacity.movie

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class MovieDataGateway(private val db: Database) {
//    fun find(email: String): UUID? = transaction(db) {
//        NotificationTable
//                .select { NotificationTable.email eq email }
//                .singleOrNull()?.get(NotificationTable.confirmationCode)
//    }

    fun save(tmdbId: Int, title: String, overview: String, posterPath: String) = transaction(db) {
        MovieTable.insert {
            it[MovieTable.tmdbId] = tmdbId
            it[MovieTable.title] = title
            it[MovieTable.overview] = overview
            it[MovieTable.posterPath] = posterPath
        }
    }
}

private object MovieTable : LongIdTable() {
    val tmdbId = integer(name = "tmdb_id")
    val title = text(name = "title")
    val overview = text(name = "overview")
    val posterPath = text(name = "poster_path")
    override val tableName = "movies"
}