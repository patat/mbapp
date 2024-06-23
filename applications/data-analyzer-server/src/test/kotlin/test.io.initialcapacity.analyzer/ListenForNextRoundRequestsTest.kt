package test.io.initialcapacity.analyzer

import io.initialcapacity.analyzer.NextRoundMessage
import io.initialcapacity.analyzer.NextRoundWorker
import io.initialcapacity.analyzer.listenForNextRoundRequests
import io.initialcapacity.model.DataGateway
import io.initialcapacity.model.Movie
import io.initialcapacity.model.Round
import io.initialcapacity.queue.MessageQueue
import org.junit.Before
import kotlin.test.Test
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI

class ListenForNextRoundRequestsTest {
    private val rabbitUrl = URI("amqp://guest:guest@${System.getenv("RABBITMQ_HOST") ?: "localhost"}:${System.getenv("RABBITMQ_PORT") ?: "5672"}")
    private lateinit var dataGatewayMock: DataGateway
    private lateinit var nextRoundQueue: MessageQueue

    @Before
    fun setUp() {
        dataGatewayMock = mockk<DataGateway>()

        nextRoundQueue = MessageQueue(
            rabbitUrl,
            exchangeName = "next-round-exchange",
            queueName = "next-round-queue"
        )

        val nextRoundWorker = NextRoundWorker(dataGatewayMock)

        listenForNextRoundRequests(nextRoundQueue, worker = nextRoundWorker)
    }

    @Test
    fun testNextRoundMessage() = runBlocking {
        val mockMovie1 = Movie(id = 54321, tmdbId=1, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovie2 = Movie(id = 54322, tmdbId=2, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovies = listOf(mockMovie1, mockMovie2)
        val mockBattleId: Long = 12345
        val mockRound = Round(id = 1, battleId = mockBattleId, movie1Id = 54321, movie2Id = 54322, winnerId = 54321)
        val message = Json.encodeToString(
            NextRoundMessage(
                battleId = mockBattleId,
                nextRoundId = 2,
                prevRoundId = 1,
                winnerId = 54321,
            )
        )
        every { dataGatewayMock.updateRound(any(), any()) } returns 1
        every { dataGatewayMock.getBattleMovies(mockBattleId) } returns mockMovies
        every { dataGatewayMock.getBattleRounds(mockBattleId) } returns listOf(mockRound)

        nextRoundQueue.publishMessage(message)
        delay(1000)

        val expectedPrevRoundUpdate = Round(
            battleId = mockBattleId,
            winnerId = 54321
        )
        val expectedNextRoundUpdate = Round(
            battleId = mockBattleId,
            winnerId = 54321
        )

        verifySequence {
            dataGatewayMock.updateRound(roundId = 1, round = expectedPrevRoundUpdate)
            dataGatewayMock.getBattleMovies(mockBattleId)
            dataGatewayMock.getBattleRounds(mockBattleId)
            dataGatewayMock.updateRound(roundId = 2, round = expectedNextRoundUpdate)
        }
    }
}
