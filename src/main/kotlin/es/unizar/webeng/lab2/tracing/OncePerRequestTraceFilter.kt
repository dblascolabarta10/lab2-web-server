package es.unizar.webeng.lab2.tracing

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filtro de Spring que se ejecuta una sola vez por petición, aunque haya
 * reenvíos internos. Devuelve el id de traza al cliente y registra
 * [TraceStep.ONCE_PER_REQUEST_IN] y [TraceStep.ONCE_PER_REQUEST_OUT].
 */
class OncePerRequestTraceFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(OncePerRequestTraceFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // Al principio, porque la cabecera no se puede añadir con la respuesta ya enviada
        MDC.get(TRACE_ID_KEY)?.let { response.setHeader(TRACE_ID_HEADER, it) }
        log.traceStep(TraceStep.ONCE_PER_REQUEST_IN, request)
        try {
            filterChain.doFilter(request, response)
        } finally {
            log.traceStep(TraceStep.ONCE_PER_REQUEST_OUT, request, response.status)
        }
    }
}
