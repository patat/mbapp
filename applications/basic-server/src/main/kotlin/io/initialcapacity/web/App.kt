package io.initialcapacity.web

import freemarker.cache.ClassTemplateLoader
import io.initialcapacity.awaiter.ResultsAwaiter
import io.initialcapacity.model.DataGateway
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.*
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*
import org.slf4j.LoggerFactory
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.encodeToString
import io.ktor.serialization.kotlinx.json.*
import java.net.URI
import io.initialcapacity.model.Movie
import io.initialcapacity.queue.MessageQueue
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.module(
    resultsAwaiter: ResultsAwaiter,
    showcaseMoviesQueue: MessageQueue,
    nextRoundQueue: MessageQueue,

) {
    val logger = LoggerFactory.getLogger(this.javaClass)

    install(ContentNegotiation) {
        json()
    }
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(Routing) {
        get("/") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("headers" to headers())))
        }
        staticResources("/static/styles", "static/styles")
        staticResources("/static/images", "static/images")
        staticResources("/static/scripts", "static/scripts")

        get("/showcase-movies") {
            val battleId = resultsAwaiter.createBattle()

            logger.info("publishing showcase movies")
            val message = Json.encodeToString(ShowcaseMoviesMessage(battleId))
            showcaseMoviesQueue.publishMessage(message)

            val movies = resultsAwaiter.waitForBattleMovies(battleId)

            call.respond(ShowcaseMoviesResponse(battleId, movies))
        }

        post("/next-round") {
            logger.info("publishing next round")
            val nextRoundRequest = call.receive<NextRoundRequest>()
            val roundId = resultsAwaiter.createNextRound(nextRoundRequest.battleId)
            val message = Json.encodeToString(
                    NextRoundMessage(
                            battleId = nextRoundRequest.battleId,
                            nextRoundId = roundId,
                            prevRoundId = nextRoundRequest.roundId,
                            winnerId = nextRoundRequest.winnerId
                    )
            )
            nextRoundQueue.publishMessage(message)

            val round = resultsAwaiter.waitForRound(roundId)

            call.respond(round)
        }
    }
}

@Serializable
data class ShowcaseMoviesMessage(
    val battleId: Long
)

@Serializable
data class ShowcaseMoviesResponse(
        val battleId: Long,
        val movies: List<Movie>
)

@Serializable
data class NextRoundRequest(
    val battleId: Long,
    val roundId: Long?,
    val winnerId: Long?,
)

@Serializable
data class NextRoundMessage(
    val battleId: Long,
    val nextRoundId: Long,
    val prevRoundId: Long?,
    val winnerId: Long?,
)

private fun PipelineContext<Unit, ApplicationCall>.headers(): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()
    call.request.headers.entries().forEach { entry ->
        headers[entry.key] = entry.value.joinToString()
    }
    return headers
}

fun webServer(
    port: Int,
    resultsAwaiter: ResultsAwaiter,
    showcaseMoviesQueue: MessageQueue,
    nextRoundQueue: MessageQueue,
) = embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module(
    resultsAwaiter,
    showcaseMoviesQueue,
    nextRoundQueue,
)})

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt() ?: 8888
    val rabbitUrl = System.getenv("CLOUDAMQP_URL")?.let(::URI)
            ?: throw RuntimeException("Please set the CLOUDAMQP_URL environment variable")

    val dbUrl = System.getenv("JDBC_DATABASE_URL")
            ?: throw RuntimeException("Please set the JDBC_DATABASE_URL environment variable")

    val dbConfig = DatabaseConfiguration(dbUrl = dbUrl)
    val dataGateway = DataGateway(dbConfig.db)
    val resultsAwaiter = ResultsAwaiter(dataGateway)

    val showcaseMoviesQueue = MessageQueue(
            rabbitUrl = rabbitUrl,
            exchangeName = "showcase-movies-exchange",
            queueName = "showcase-movies-queue"
    )

    val nextRoundQueue = MessageQueue(
            rabbitUrl = rabbitUrl,
            exchangeName = "next-round-exchange",
            queueName = "next-round-queue"
    )

    webServer(
        port,
        resultsAwaiter,
        showcaseMoviesQueue,
        nextRoundQueue
    ).start(wait = true)
}
