package io.initialcapacity.web

import freemarker.cache.ClassTemplateLoader
import io.initialcapacity.awaiter.ResultsAwaiter
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.response.*
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*
import org.slf4j.LoggerFactory
import io.initialcapacity.rabbitsupport.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.encodeToString
import io.ktor.serialization.kotlinx.json.*
import java.net.URI
import io.initialcapacity.model.Movie
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.module() {
    val logger = LoggerFactory.getLogger(this.javaClass)

    val rabbitUrl = System.getenv("CLOUDAMQP_URL")?.let(::URI)
            ?: throw RuntimeException("Please set the CLOUDAMQP_URL environment variable")

    val dbUrl = System.getenv("JDBC_DATABASE_URL")
            ?: throw RuntimeException("Please set the JDBC_DATABASE_URL environment variable")

    val dbConfig = DatabaseConfiguration(dbUrl = dbUrl)

    val connectionFactory = buildConnectionFactory(rabbitUrl)
    val moviesExchange = RabbitExchange(
            name = "movies-exchange",
            type = "direct",
            routingKeyGenerator = { _: String -> "42" },
            bindingKey = "42",
    )
    val collectMoviesQueue = RabbitQueue("collect-movies")
    connectionFactory.declareAndBind(exchange = moviesExchange, queue = collectMoviesQueue)

    val battlesExchange = RabbitExchange(
            name = "battles-exchange",
            type = "direct",
            routingKeyGenerator = { _: String -> "42" },
            bindingKey = "42",
    )
    val showcaseMoviesQueue = RabbitQueue("showcase-movies")
    connectionFactory.declareAndBind(exchange = battlesExchange, queue = showcaseMoviesQueue)

    val roundsExchange = RabbitExchange(
            name = "rounds-exchange",
            type = "direct",
            routingKeyGenerator = { _: String -> "42" },
            bindingKey = "42",
    )
    val roundsQueue = RabbitQueue("next-round")
    connectionFactory.declareAndBind(exchange = roundsExchange, queue = roundsQueue)

    val resultsAwaiter = ResultsAwaiter(dbConfig.db)

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

        get("/collect-movies") {
            val publishCollectMovies = publish(connectionFactory, moviesExchange)

            logger.debug("publishing collect movies")
            publishCollectMovies("collect movies")

            call.respondText("collect movies published!", ContentType.Text.Html)
        }

        get("/showcase-movies") {
            val battleId = resultsAwaiter.createBattle()

            logger.debug("publishing showcase movies")
            val publishShowcaseMovies = publish(connectionFactory, battlesExchange)
            val message = Json.encodeToString(ShowcaseMoviesMessage(battleId))
            publishShowcaseMovies(message)

            val movies = resultsAwaiter.waitForBattleMovies(battleId)

            call.respond(ShowcaseMoviesResponse(battleId, movies))
        }

        post("/next-round") {
            logger.debug("publishing next round")
            val nextRoundRequest = call.receive<NextRoundRequest>()
            val roundId = resultsAwaiter.createNextRound(nextRoundRequest.battleId)

            val publishNextRound = publish(connectionFactory, roundsExchange)
            val message = Json.encodeToString(
                    NextRoundMessage(
                            battleId = nextRoundRequest.battleId,
                            nextRoundId = roundId,
                            prevRoundId = nextRoundRequest.roundId,
                            winnerId = nextRoundRequest.winnerId
                    )
            )
            publishNextRound(message)

            val round = resultsAwaiter.waitForRound(roundId)

            call.respond(round)
        }
    }
}

@Serializable
private data class ShowcaseMoviesMessage(
      val battleId: Long
)

@Serializable
private data class ShowcaseMoviesResponse(
        val battleId: Long,
        val movies: List<Movie>
)

@Serializable
private data class NextRoundRequest(
        val battleId: Long,
        val roundId: Long?,
        val winnerId: Long?,
)

@Serializable
private data class NextRoundMessage(
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

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt() ?: 8888
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module() }).start(wait = true)
}
