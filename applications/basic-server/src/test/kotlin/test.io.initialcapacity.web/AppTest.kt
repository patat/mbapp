package test.io.initialcapacity.web

import io.initialcapacity.awaiter.ResultsAwaiter
import io.initialcapacity.model.DataGateway
import io.initialcapacity.model.Movie
import io.initialcapacity.model.Round
import io.initialcapacity.queue.MessageQueue
import io.initialcapacity.web.*
import io.ktor.client.*
import io.ktor.client.engine.java.*
import org.junit.Before
import kotlin.test.Test
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.netty.*
import org.junit.After
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppTest {
    private val port = 8888
    private val client = HttpClient(Java) {
        expectSuccess = false
    }
    private lateinit var server: NettyApplicationEngine
    private lateinit var dataGatewayMock: DataGateway
    private lateinit var showcaseMoviesQueueMock: MessageQueue
    private lateinit var nextRoundQueueMock: MessageQueue

    @Before
    fun setUp() {
        dataGatewayMock = mockk<DataGateway>()
        val resultsAwaiter = ResultsAwaiter(dataGatewayMock)
        showcaseMoviesQueueMock = mockk<MessageQueue>()
        nextRoundQueueMock = mockk<MessageQueue>()
        val collectMoviesQueueMock = mockk<MessageQueue>()

        server = webServer(port, resultsAwaiter, showcaseMoviesQueueMock, nextRoundQueueMock, collectMoviesQueueMock)

        server.start(wait = false)
    }

    @After
    fun tearDown() {
        server.stop(50, 50)
    }

    @Test
    fun testRootOk() = runBlocking {
        val response = client.get("http://localhost:$port/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(),"Let movies fight to be watched")
    }

    @Test
    fun testShowcaseMovies() = runBlocking {
        val mockMovie = Movie(id = 54321, tmdbId=1, title="Test movie", overview="Test overview", posterPath="/test-poster");
        val mockBattleId: Long = 12345;
        every { dataGatewayMock.createBattle() } returns mockBattleId
        every { dataGatewayMock.getBattleMovies(mockBattleId) } returns listOf(mockMovie)
        every { showcaseMoviesQueueMock.getProperty("publishMessage") } returns {
            message: String -> assertEquals(message, Json.encodeToString(ShowcaseMoviesMessage(battleId = mockBattleId)))
        }

        val response = client.get("http://localhost:$port/showcase-movies")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            Json.encodeToString(ShowcaseMoviesResponse(battleId = mockBattleId, movies = listOf(mockMovie))),
            response.bodyAsText(),
        )
    }

//    @Test
//    fun testNextRound() = runBlocking {
//        val mockBattleId: Long = 12345
//        val mockRoundId: Long = 1;
//        val mockRound = Round(id = mockRoundId, battleId = mockBattleId, movie1Id = 12, movie2Id = 13, winnerId = 12);
//
//        every { dataGatewayMock.createRound(mockBattleId) } returns mockRoundId
//        every { dataGatewayMock.getRoundById(mockRoundId) } returns mockRound
//        every { nextRoundQueueMock.getProperty("publishMessage") } returns {
//            message: String -> assertEquals(message, Json.encodeToString(
//                NextRoundMessage(
//                        battleId = mockBattleId,
//                        nextRoundId = mockRoundId,
//                        prevRoundId = null,
//                        winnerId = null,
//                )
//            ))
//        }
//
//        val response = client.post("http://localhost:$port/next-round") {
//            headers {
//                contentType(ContentType.Application.Json)
//                setBody(
//                        """{"battleId": $mockBattleId, "roundId": null, "winnerId": null}"""
//                )
//            }
//        }
//
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertEquals(
//            Json.encodeToString(mockRound),
//            response.bodyAsText()
//        )
//    }
}
