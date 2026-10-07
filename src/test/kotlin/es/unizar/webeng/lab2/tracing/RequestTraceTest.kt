package es.unizar.webeng.lab2.tracing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import tools.jackson.databind.json.JsonMapper
import java.io.File

/**
 * Hace una petición real a `/time` y comprueba en el fichero de logs JSON
 * que ha pasado por todos los pasos de la traza, en orden.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class RequestTraceTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var client: TestRestTemplate

    // El mismo fichero que configura src/test/resources/application.yml
    @Value("\${logging.file.name}")
    private lateinit var logFile: String

    private val mapper = JsonMapper.builder().build()

    @Test
    fun timeRequestLeavesFullTraceInLogFile() {
        val response = client.getForEntity("http://127.0.0.1:$port/time", String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        // Cabecera del OncePerRequestFilter: con ella se encuentran las líneas de esta petición
        val traceId = response.headers.getFirst(TRACE_ID_HEADER)
        assertNotNull(traceId)
        // Cabecera del ResponseBodyAdvice
        assertEquals("TimeController.time", response.headers.getFirst(HANDLER_HEADER))

        val steps = waitForTrace(traceId!!)

        // El orden del enum es el orden esperado de ejecución
        assertEquals(TraceStep.entries.map { it.name }, steps)
    }

    // FILTER_OUT puede escribirse justo después de que llegue la respuesta, así que se espera un poco
    private fun waitForTrace(traceId: String): List<String> {
        val deadline = System.currentTimeMillis() + 5_000
        var steps = stepsOf(traceId)
        while (steps.lastOrNull() != TraceStep.FILTER_OUT.name && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
            steps = stepsOf(traceId)
        }
        return steps
    }

    // Lee el fichero y devuelve, en orden, los pasos con ese id de traza
    private fun stepsOf(traceId: String): List<String> {
        val file = File(logFile)
        if (!file.exists()) return emptyList()
        return file
            .readLines()
            .filter { it.contains(traceId) }
            // Si una línea aún se está escribiendo no es JSON válido: se ignora
            .mapNotNull { line -> runCatching { mapper.readTree(line) }.getOrNull() }
            .filter { it.path(TRACE_ID_KEY).asString() == traceId && it.has("step") }
            .map { it.path("step").asString() }
    }
}
