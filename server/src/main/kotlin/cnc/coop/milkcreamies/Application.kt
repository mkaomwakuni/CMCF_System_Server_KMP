package cnc.coop.milkcreamies

import cnc.coop.milkcreamies.data.DatabaseConfig
import cnc.coop.milkcreamies.plugins.configurePlugins
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0") {
        DatabaseConfig.init()
        configurePlugins()
    }.start(wait = true)
}
