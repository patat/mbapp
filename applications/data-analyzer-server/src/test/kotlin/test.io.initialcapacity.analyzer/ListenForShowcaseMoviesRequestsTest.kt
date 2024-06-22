package test.io.initialcapacity.analyzer

import io.initialcapacity.analyzer.*
import io.initialcapacity.model.DataGateway
import io.initialcapacity.model.Movie
import io.initialcapacity.queue.MessageQueue
import org.junit.Before
import kotlin.test.Test
import io.mockk.*
import kotlinx.coroutines.delay
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI

class ListenForShowcaseMoviesRequestsTest {
    private val rabbitUrl = URI("amqp://localhost:5672")
    private lateinit var dataGatewayMock: DataGateway
    private lateinit var collectMoviesQueueMock: MessageQueue
    private lateinit var showcaseMoviesQueue: MessageQueue

    @Before
    fun setUp() {
        dataGatewayMock = mockk<DataGateway>()
        collectMoviesQueueMock = mockk<MessageQueue>()

        showcaseMoviesQueue = MessageQueue(
                rabbitUrl,
                exchangeName = "showcase-movies-exchange",
                queueName = "showcase-movies-queue"
        )

        val showcaseMoviesWorker = ShowcaseMoviesWorker(dataGatewayMock, collectMoviesQueueMock)

        listenForShowcaseMoviesRequests(showcaseMoviesQueue, worker = showcaseMoviesWorker)
    }

    @Test
    fun testShowcaseMoviesMessage() = runBlocking {
        val mockMovie = Movie(id = 54321, tmdbId=1, title="Test movie", overview="Test overview", posterPath="/test-poster");
        val mockBattleId: Long = 12345;
        val message = Json.encodeToString(ShowcaseMoviesMessage(mockBattleId))

        every { dataGatewayMock.getFreshMovies()} returns listOf(mockMovie)
        every { dataGatewayMock.addMoviesToBattle(any(), any()) } returns Unit
        every { collectMoviesQueueMock.getProperty("publishMessage") } returns {
            collectMessage: String -> assertEquals("collect movies", collectMessage)
        }

        showcaseMoviesQueue.publishMessage(message)
        delay(1000)

        verify { dataGatewayMock.addMoviesToBattle(battleId = mockBattleId, any()) }
        verify { dataGatewayMock.getFreshMovies() }
    }
}
