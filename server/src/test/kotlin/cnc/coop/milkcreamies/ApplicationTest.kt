package cnc.coop.milkcreamies

import cnc.coop.milkcreamies.data.DatabaseConfig
import cnc.coop.milkcreamies.plugins.configurePlugins
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testHealth() = testApplication {
        application {
            DatabaseConfig.init()
            configurePlugins()
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Server is running!", response.bodyAsText())
    }

    @Test
    fun testUserSignup() = testApplication {
        application {
            DatabaseConfig.init()
            configurePlugins()
        }
        val response = client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "password123",
                    "firstName": "Test",
                    "lastName": "User"
                }
            """.trimIndent()
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }
}
