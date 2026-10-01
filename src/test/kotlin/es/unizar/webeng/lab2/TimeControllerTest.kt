package es.unizar.webeng.lab2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.HttpURLConnection
import java.net.URL

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TimeControllerTest {
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun timeIsJson() {
        val url = URL("http://127.0.0.1:$port/time")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        assertEquals(200, connection.responseCode)

        val response = connection.inputStream.bufferedReader().readText()
        assertTrue(response.contains("\"time\""))
    }
}
