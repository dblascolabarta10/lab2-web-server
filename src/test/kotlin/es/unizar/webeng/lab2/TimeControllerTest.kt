package es.unizar.webeng.lab2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TimeControllerTest {
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun timeIsJson() {
        // Usamos URI en lugar del constructor deprecado de URL
        val url = URI("http://127.0.0.1:$port/time").toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        assertEquals(200, connection.responseCode)

        val response = connection.inputStream.bufferedReader().readText()
        assertTrue(response.contains("\"time\""))
    }

    @Test
    fun `should return exact timestamp with fixed provider`() {
        // Creamos un tiempo estático y exacto
        val fixedTimestamp = LocalDateTime.of(2026, 10, 1, 15, 30, 0)

        // Creamos un proveedor falso (stub) que siempre devuelva ese tiempo
        val fixedProvider =
            object : TimeProvider {
                override fun now(): LocalDateTime = fixedTimestamp
            }

        // Inyectamos el proveedor falso directamente en el controlador (sin Spring)
        val controller = TimeController(fixedProvider)

        // Comprobamos que el JSON (DTO) devuelve exactamente ese tiempo
        assertEquals(fixedTimestamp, controller.time().time)
    }
}
