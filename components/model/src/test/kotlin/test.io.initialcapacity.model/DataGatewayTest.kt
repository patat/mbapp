import io.initialcapacity.model.DataGateway
import io.initialcapacity.model.Movie
import io.initialcapacity.model.Round
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class DataGatewayTest() {
    private val db by lazy {
        Database.connect(
                url = "jdbc:postgresql://localhost:5432/mb_test?user=mbapp&password=mbapp_password"
        )
    }
    private val gateway = DataGateway(db)

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {
        transaction(db) {
            exec("delete from movies_battles_relation")
            exec("delete from rounds")
            exec("delete from battles")
            exec("delete from movies")
        }
    }
    @Test
    fun getFreshMoviesTest() {
        val mockMovies = IntRange(1, 17).map {
            Movie(
                id = it.toLong(),
                tmdbId = it + 10,
                title = "Test movie",
                overview = "Test overview",
                posterPath = "/poster-path"
            )
        }

        transaction(db) {
            mockMovies.forEach {
                val insertMovieStmt = """
                    insert into movies (id, tmdb_id, title, overview, poster_path) 
                    values(${it.id}, ${it.tmdbId}, '${it.title}', '${it.overview}', '${it.posterPath}')
                """.trimIndent()
                exec(insertMovieStmt)
            }

            val insertBallteStmt = """
                insert into battles (id, winner_id)
                values(1, ${mockMovies[0].id})
            """.trimIndent()
            exec(insertBallteStmt)

            val insertMovieBallteRelationStmt = """
                insert into movies_battles_relation (movie_id, battle_id)
                values(${mockMovies[0].id}, 1)
            """.trimIndent()
            exec(insertMovieBallteRelationStmt)
        }

        val movies = gateway.getFreshMovies()

        assertEquals(16, movies.size)

        movies.forEachIndexed { index, movie ->
            assertEquals(mockMovies[index + 1].tmdbId, movie.tmdbId)
        }
    }

    @Test
    fun saveMovieNewTest() {
        val mockMovie = Movie(
                id = 1,
                tmdbId = 11,
                title = "Test movie",
                overview = "Test overview",
                posterPath = "/poster-path"
        )

        transaction {
            val insertMovieStmt = """
                    insert into movies (id, tmdb_id, title, overview, poster_path) 
                    values(${mockMovie.id}, ${mockMovie.tmdbId}, 'Old Title', 'Old overview', '/old-path')
                """.trimIndent()
            exec(insertMovieStmt)
        }


        gateway.saveMovie(mockMovie)

        val savedMovie = gateway.getMovieByTmdbId(11)

        assertEquals(mockMovie.tmdbId, savedMovie.tmdbId)
        assertEquals(mockMovie.title, savedMovie.title)
        assertEquals(mockMovie.overview, savedMovie.overview)
        assertEquals(mockMovie.posterPath, savedMovie.posterPath)
    }

    @Test
    fun saveMovieUpdateTest() {
        val mockMovie = Movie(
                id = 1,
                tmdbId = 11,
                title = "Test movie",
                overview = "Test overview",
                posterPath = "/poster-path"
        )

        gateway.saveMovie(mockMovie)

        val savedMovie = gateway.getMovieByTmdbId(11)

        assertEquals(mockMovie.tmdbId, savedMovie.tmdbId)
        assertEquals(mockMovie.title, savedMovie.title)
        assertEquals(mockMovie.overview, savedMovie.overview)
        assertEquals(mockMovie.posterPath, savedMovie.posterPath)
    }

    @Test
    fun battleMoviesTest() {
        val mockMovies = IntRange(1, 16).map {
            Movie(
                    id = it.toLong(),
                    tmdbId = it + 10,
                    title = "Test movie",
                    overview = "Test overview",
                    posterPath = "/poster-path"
            )
        }

        transaction(db) {
            mockMovies.forEach {
                val insertMovieStmt = """
                    insert into movies (id, tmdb_id, title, overview, poster_path) 
                    values(${it.id}, ${it.tmdbId}, '${it.title}', '${it.overview}', '${it.posterPath}')
                """.trimIndent()
                exec(insertMovieStmt)
            }
        }

        val battleId = gateway.createBattle()
        gateway.addMoviesToBattle(battleId, mockMovies.map { it.id })

        val battleMovies = gateway.getBattleMovies(battleId)

        assertEquals(mockMovies, battleMovies)
    }

    @Test
    fun roundTest() {
        val mockMovies = IntRange(1, 2).map {
            Movie(
                    id = it.toLong(),
                    tmdbId = it + 10,
                    title = "Test movie",
                    overview = "Test overview",
                    posterPath = "/poster-path"
            )
        }

        transaction(db) {
            mockMovies.forEach {
                val insertMovieStmt = """
                    insert into movies (id, tmdb_id, title, overview, poster_path) 
                    values(${it.id}, ${it.tmdbId}, '${it.title}', '${it.overview}', '${it.posterPath}')
                """.trimIndent()
                exec(insertMovieStmt)
            }
        }

        val battleId = gateway.createBattle()
        val roundId = gateway.createRound(battleId)

        val mockRound = Round(
            id = roundId,
            battleId = battleId,
            movie1Id = mockMovies[0].id,
            movie2Id = mockMovies[1].id,
            winnerId = mockMovies[0].id
        )

        gateway.updateRound(roundId, mockRound)

        val savedRound = gateway.getRoundById(roundId)

        assertEquals(mockRound, savedRound)
    }

    @Test
    fun getBattleRoundsTest() {
        val battleId = gateway.createBattle()

        val mockMovies = IntRange(1, 4).map {
            Movie(
                    id = it.toLong(),
                    tmdbId = it + 10,
                    title = "Test movie",
                    overview = "Test overview",
                    posterPath = "/poster-path"
            )
        }

        val mockRounds = IntRange(1, 2).map {
            Round(
                    id = it.toLong(),
                    battleId = battleId,
                    movie1Id = mockMovies[2*it - 2].id,
                    movie2Id = mockMovies[2*it - 1].id,
                    winnerId = mockMovies[2*it - 2].id
            )
        }

        transaction(db) {
            mockMovies.forEach {
                val insertMovieStmt = """
                    insert into movies (id, tmdb_id, title, overview, poster_path) 
                    values(${it.id}, ${it.tmdbId}, '${it.title}', '${it.overview}', '${it.posterPath}')
                """.trimIndent()
                exec(insertMovieStmt)
            }

            mockRounds.forEach {
                val insertRoundsStmt = """
                    insert into rounds (id, battle_id, movie1_id, movie2_id, winner_id) 
                    values(${it.id}, ${it.battleId}, '${it.movie1Id}', '${it.movie2Id}', '${it.winnerId}')
                """.trimIndent()
                exec(insertRoundsStmt)
            }
        }

        val battleRounds = gateway.getBattleRounds(battleId)

        assertEquals(mockRounds, battleRounds)
    }
}