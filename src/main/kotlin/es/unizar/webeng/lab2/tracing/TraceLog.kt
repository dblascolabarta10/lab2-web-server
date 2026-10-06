package es.unizar.webeng.lab2.tracing

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger

/** Clave del id de traza en el MDC; aparece como campo `traceId` en el JSON. */
const val TRACE_ID_KEY = "traceId"

/** Cabecera con la que el cliente recibe el id de traza. */
const val TRACE_ID_HEADER = "X-Trace-Id"

/**
 * Escribe un paso de la traza. El paso va como campo propio del JSON
 * para que el test pueda buscarlo sin interpretar el mensaje.
 */
fun Logger.traceStep(
    step: TraceStep,
    request: HttpServletRequest,
    status: Int? = null,
) = traceStep(step, "${request.method} ${request.requestURI}", status)

/** Variante para sitios sin acceso a la petición, como el controlador. */
fun Logger.traceStep(
    step: TraceStep,
    detail: String,
    status: Int? = null,
) {
    var event = atInfo().addKeyValue("step", step.name)
    if (status != null) event = event.addKeyValue("status", status)
    event.log("{} {}", step, detail)
}
