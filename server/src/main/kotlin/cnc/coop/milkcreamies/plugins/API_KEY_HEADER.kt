package cnc.coop.milkcreamies.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

private const val API_KEY_HEADER = "X-API-Key"
private const val VALID_API_KEY = "dairy-app-secret-key-12345"
private val logger = LoggerFactory.getLogger("ApiKeyAuth")

fun ApplicationCall.authenticateApiKey(): Boolean {
    val apiKey = request.headers[API_KEY_HEADER]
    return if (apiKey == VALID_API_KEY) {
        true
    } else {
        logger.warn("Invalid or missing API key: $apiKey")
        false
    }
}

suspend fun ApplicationCall.respondUnauthorized() {
    respond(
        HttpStatusCode.Unauthorized,
        mapOf("error" to "Invalid or missing API key")
    )
}