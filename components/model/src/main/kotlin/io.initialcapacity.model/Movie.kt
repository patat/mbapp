package io.initialcapacity.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow

@Serializable
data class Movie(
        val id: Long,
        val tmdbId: Int,
        val title: String,
        val overview: String,
        val posterPath: String
)

object MovieTable : LongIdTable("movies") {
    val tmdbId = integer(name = "tmdb_id")
    val title = text(name = "title")
    val overview = text(name = "overview")
    val posterPath = text(name = "poster_path")
}

fun ResultRow.toMovie() = Movie(
        id = this[MovieTable.id].value,
        tmdbId = this[MovieTable.tmdbId],
        title = this[MovieTable.title],
        overview = this[MovieTable.overview],
        posterPath = this[MovieTable.posterPath]
)