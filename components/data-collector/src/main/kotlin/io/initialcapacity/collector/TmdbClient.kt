import info.movito.themoviedbapi.TmdbApi
import io.initialcapacity.model.Movie
import info.movito.themoviedbapi.tools.builders.discover.DiscoverMovieParamBuilder

class TmdbClient (tmdbApiKey: String) {
    private val movieDbClient = TmdbApi(tmdbApiKey)

    fun getMovies(): List<Movie> {
        val tmdbDiscover = movieDbClient.discover

        val params = DiscoverMovieParamBuilder()
        params.language("en-US")
        params.page((0..500).random())

        return tmdbDiscover.getMovie(params).results.map {
            Movie(
                id = 1,
                tmdbId = it.id,
                title = it.title,
                overview = it.overview,
                posterPath = it.posterPath
            )
        }
    }
}