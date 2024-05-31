package io.initialcapacity.collector

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.tools.builders.discover.DiscoverMovieParamBuilder
import io.initialcapacity.model.DataGateway
import org.jetbrains.exposed.sql.Database
import java.lang.RuntimeException

class CollectMoviesWorker(private val db: Database) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun execute() {
        runBlocking {
            val tmdbApiKey = System.getenv("TMDB_API_KEY")
                    ?: throw RuntimeException("Please set the TMDB_API_KEY environment variable")

            logger.info("starting data collection.")

            val movieDbClient = TmdbApi(tmdbApiKey)

            val params = DiscoverMovieParamBuilder()
            params.language("en-US")
            params.page((0..500).random())

            val tmdbDiscover = movieDbClient.discover
            val movies = tmdbDiscover.getMovie(params)

            val gateway = DataGateway(db)

            movies.results.forEach {
                gateway.saveMovie(tmdbId = it.id, title = it.title, overview = it.overview, posterPath = it.posterPath)
            }

            logger.info("completed data collection. ${movies.toString()}")
        }
    }
}