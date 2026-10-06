package es.unizar.webeng.lab2

import es.unizar.webeng.lab2.tracing.TraceStep
import es.unizar.webeng.lab2.tracing.traceStep
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

// DTO para la respuesta JSON
data class TimeDTO(
    val time: LocalDateTime,
)

// Interfaz del proveedor
interface TimeProvider {
    fun now(): LocalDateTime
}

// Implementación del servicio
@Service
class TimeService : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}

// Función de extensión
fun LocalDateTime.toDTO(): TimeDTO = TimeDTO(time = this)

// Controlador REST
@RestController
class TimeController(
    private val service: TimeProvider,
) {
    private val log = LoggerFactory.getLogger(TimeController::class.java)

    @GetMapping("/time")
    fun time(): TimeDTO {
        // Punto central de la traza: aquí llega la petición
        log.traceStep(TraceStep.HANDLER, "TimeController.time")
        return service.now().toDTO()
    }
}
