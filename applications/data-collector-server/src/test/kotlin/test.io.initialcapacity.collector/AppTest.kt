package test.io.initialcapacity.collector

import TmdbClient
import io.initialcapacity.collector.start
import io.initialcapacity.model.DataGateway
import io.initialcapacity.model.Movie
import io.initialcapacity.queue.MessageQueue
import org.junit.Before
import kotlin.test.Test
import io.mockk.*
import kotlinx.coroutines.delay
import org.junit.After
import kotlinx.coroutines.runBlocking
import java.net.URI

class AppTest {
    private val rabbitUrl = URI("amqp://localhost:5672")
    private lateinit var dataGatewayMock: DataGateway
    private lateinit var collectMoviesQueue: MessageQueue
    private lateinit var tmdbClientMock: TmdbClient

    @Before
    fun setUp() {
        dataGatewayMock = mockk<DataGateway>()
        tmdbClientMock = mockk<TmdbClient>()


        collectMoviesQueue = MessageQueue(
            rabbitUrl,
            exchangeName = "collect-movies-exchange",
            queueName = "collect-movies-queue"
        )

        start(
            collectMoviesQueue,
            gateway = dataGatewayMock,
            tmdbClient = tmdbClientMock,
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun testCollectMoviesMessage() = runBlocking {
        val mockMovie1 = Movie(id = 54321, tmdbId=1, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovie2 = Movie(id = 54322, tmdbId=2, title="Test movie", overview="Test overview", posterPath="/test-poster")

        every { dataGatewayMock.saveMovie(any()) } returns 1
        every { tmdbClientMock.getMovies() } returns listOf(mockMovie1, mockMovie2)

        collectMoviesQueue.publishMessage("collect movies")
        delay(1000)

        verifySequence {
            tmdbClientMock.getMovies()
            dataGatewayMock.saveMovie(mockMovie1)
            dataGatewayMock.saveMovie(mockMovie2)
        }
    }
}
