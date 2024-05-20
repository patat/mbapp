package io.initialcapacity.web

import freemarker.cache.ClassTemplateLoader
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
import java.util.*
import org.slf4j.LoggerFactory
import io.initialcapacity.rabbitsupport.*
import java.net.URI

fun Application.module() {
    val logger = LoggerFactory.getLogger(this.javaClass)

    val rabbitUrl = System.getenv("RABBIT_URL")?.let(::URI)
            ?: throw RuntimeException("Please set the RABBIT_URL environment variable")

    val connectionFactory = buildConnectionFactory(rabbitUrl)
    val collectMoviesExchange = RabbitExchange(
            name = "collect-movies-exchange",
            type = "direct",
            routingKeyGenerator = { _: String -> "42" },
            bindingKey = "42",
    )
    val collectMoviesQueue = RabbitQueue("collect-movies")
    connectionFactory.declareAndBind(exchange = collectMoviesExchange, queue = collectMoviesQueue)

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
            val publishCollectMovies = publish(connectionFactory, collectMoviesExchange)

            logger.debug("publishing collect movies")
            publishCollectMovies("collect movies")

            call.respondText("collect movies published!", ContentType.Text.Html)
        }
    }
}

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
