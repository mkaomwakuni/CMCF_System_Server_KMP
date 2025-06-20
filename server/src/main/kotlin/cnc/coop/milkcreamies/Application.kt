package cnc.coop.milkcreamies

import cnc.coop.milkcreamies.data.DatabaseConfig
import cnc.coop.milkcreamies.plugins.configurePlugins
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        DatabaseConfig.init()
        configurePlugins()
    }.start(wait = true)
}

