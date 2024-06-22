package test.io.initialcapacity.analyzer

import io.initialcapacity.analyzer.calculateNextRound
import io.initialcapacity.model.Movie
import io.initialcapacity.model.Round
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateNextRoundTest {
    @Test
    fun testFirstRound() {
        val mockBattleId: Long = 12345
        val mockMovie1 = Movie(id = 54321, tmdbId=1, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovie2 = Movie(id = 54322, tmdbId=2, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovies = listOf(mockMovie1, mockMovie2)

        val expected = Round(battleId = mockBattleId, movie1Id = mockMovie1.id, movie2Id = mockMovie2.id)
        val actual = calculateNextRound(
            battleId = mockBattleId,
            battleMovies = mockMovies,
            battleRounds = listOf()
        )

        assertEquals(expected, actual)
    }

    @Test
    fun testOnStageRound() {
        val mockBattleId: Long = 12345
        val mockMovie1 = Movie(id = 54321, tmdbId=1, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovie2 = Movie(id = 54322, tmdbId=2, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovie3 = Movie(id = 54323, tmdbId=3, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovie4 = Movie(id = 54324, tmdbId=4, title="Test movie", overview="Test overview", posterPath="/test-poster")

        val mockMovies = listOf(mockMovie1, mockMovie2, mockMovie3, mockMovie4)

        val mockRound1 = Round(
            battleId = mockBattleId,
            movie1Id = mockMovie1.id,
            movie2Id = mockMovie2.id,
            winnerId = mockMovie1.id
        )

        val expected = Round(battleId = mockBattleId, movie1Id = mockMovie3.id, movie2Id = mockMovie4.id)
        val actual = calculateNextRound(
                battleId = mockBattleId,
                battleMovies = mockMovies,
                battleRounds = listOf(mockRound1)
        )

        assertEquals(expected, actual)
    }

    @Test
    fun testNextStageRound() {
        val mockBattleId: Long = 12345
        val mockMovie1 = Movie(id = 54321, tmdbId=1, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovie2 = Movie(id = 54322, tmdbId=2, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovie3 = Movie(id = 54323, tmdbId=3, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovie4 = Movie(id = 54324, tmdbId=4, title="Test movie", overview="Test overview", posterPath="/test-poster")

        val mockMovies = listOf(mockMovie1, mockMovie2, mockMovie3, mockMovie4)

        val mockRound1 = Round(
                battleId = mockBattleId,
                movie1Id = mockMovie1.id,
                movie2Id = mockMovie2.id,
                winnerId = mockMovie1.id
        )

        val mockRound2 = Round(
                battleId = mockBattleId,
                movie1Id = mockMovie3.id,
                movie2Id = mockMovie4.id,
                winnerId = mockMovie4.id
        )

        val expected = Round(battleId = mockBattleId, movie1Id = mockMovie1.id, movie2Id = mockMovie4.id)
        val actual = calculateNextRound(
                battleId = mockBattleId,
                battleMovies = mockMovies,
                battleRounds = listOf(mockRound1, mockRound2)
        )

        assertEquals(expected, actual)
    }

    @Test
    fun testHasWinnerRound() {
        val mockBattleId: Long = 12345
        val mockMovie1 = Movie(id = 54321, tmdbId=1, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovie2 = Movie(id = 54322, tmdbId=2, title="Test movie", overview="Test overview", posterPath="/test-poster")
        val mockMovies = listOf(mockMovie1, mockMovie2)

        val expected = Round(battleId = mockBattleId, winnerId = mockMovie1.id)
        val actual = calculateNextRound(
                battleId = mockBattleId,
                battleMovies = mockMovies,
                battleRounds = listOf(Round(
                        battleId = mockBattleId,
                        movie1Id = mockMovie1.id,
                        movie2Id = mockMovie2.id,
                        winnerId = mockMovie1.id,
                ))
        )

        assertEquals(expected, actual)
    }
}
