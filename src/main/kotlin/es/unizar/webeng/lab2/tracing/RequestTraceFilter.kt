package es.unizar.webeng.lab2.tracing

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.util.UUID

/**
 * Filtro servlet puro: es lo primero que ve la petición al entrar al contenedor
 * y lo último al salir. Crea el id de traza y registra
 * [TraceStep.FILTER_IN] y [TraceStep.FILTER_OUT].
 */
class RequestTraceFilter : Filter {
    private val log = LoggerFactory.getLogger(RequestTraceFilter::class.java)

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        val http = request as HttpServletRequest
        // Se crea aquí, en la capa más externa, para que todos los pasos lo lleven.
        // En el reenvío a /error la petición es la misma, así que se reutiliza su id.
        val traceId = http.getAttribute(TRACE_ID_KEY) as String? ?: UUID.randomUUID().toString()
        http.setAttribute(TRACE_ID_KEY, traceId)
        MDC.put(TRACE_ID_KEY, traceId)
        log.traceStep(TraceStep.FILTER_IN, http)
        try {
            chain.doFilter(request, response)
        } finally {
            // Se registra aunque falle algo más adentro
            log.traceStep(TraceStep.FILTER_OUT, http, (response as HttpServletResponse).status)
            // El hilo se reutiliza para otras peticiones: no puede quedar el id
            MDC.remove(TRACE_ID_KEY)
        }
    }
}
