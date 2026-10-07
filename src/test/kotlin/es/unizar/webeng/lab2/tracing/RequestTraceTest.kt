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
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import tools.jackson.databind.json.JsonMapper
import java.io.File

/**
 * Hace peticiones reales y comprueba en el fichero de logs JSON
 * que han pasado por todos los pasos de la traza, en orden.
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

        // El orden del enum es el orden esperado de ejecución
        val expected = TraceStep.entries.map { it.name }
        assertEquals(expected, waitForTrace(traceId!!, expected.size))
    }

    @Test
    fun unknownPathTracesErrorForwardWithSameId() {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.TEXT_HTML)
        val response =
            client.exchange(
                "http://127.0.0.1:$port/missing",
                HttpMethod.GET,
                HttpEntity<Void>(headers),
                String::class.java,
            )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val traceId = response.headers.getFirst(TRACE_ID_HEADER)
        assertNotNull(traceId)

        // Primero la petición original, que falla sin llegar a ningún controlador.
        // Después el reenvío a /error: el Filter se repite y el OncePerRequestFilter no.
        val expected =
            listOf(
                TraceStep.FILTER_IN,
                TraceStep.ONCE_PER_REQUEST_IN,
                TraceStep.PRE_HANDLE,
                TraceStep.AFTER_COMPLETION,
                TraceStep.ONCE_PER_REQUEST_OUT,
                TraceStep.FILTER_OUT,
                TraceStep.FILTER_IN,
                TraceStep.PRE_HANDLE,
                TraceStep.POST_HANDLE,
                TraceStep.AFTER_COMPLETION,
                TraceStep.FILTER_OUT,
            ).map { it.name }
        assertEquals(expected, waitForTrace(traceId!!, expected.size))
    }

    // Los últimos pasos pueden escribirse justo después de que llegue la respuesta, así que se espera un poco
    private fun waitForTrace(
        traceId: String,
        expectedSize: Int,
    ): List<String> {
        val deadline = System.currentTimeMillis() + 5_000
        var steps = stepsOf(traceId)
        while (steps.size < expectedSize && System.currentTimeMillis() < deadline) {
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
