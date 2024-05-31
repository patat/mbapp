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
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
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

fun Application.module() {
    val logger = LoggerFactory.getLogger(this.javaClass)

    val rabbitUrl = System.getenv("RABBIT_URL")?.let(::URI)
            ?: throw RuntimeException("Please set the RABBIT_URL environment variable")

    val dbUrl = System.getenv("DATABASE_URL")
            ?: throw RuntimeException("Please set the DATABASE_URL environment variable")

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

            call.respond(movies)
        }
    }
}

@Serializable
private data class ShowcaseMoviesMessage(
      val battleId: Long
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
