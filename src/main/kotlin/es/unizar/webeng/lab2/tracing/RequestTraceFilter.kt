package es.unizar.webeng.lab2.tracing

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory

/**
 * Filtro servlet puro: es lo primero que ve la petición al entrar al contenedor
 * y lo último al salir. Registra [TraceStep.FILTER_IN] y [TraceStep.FILTER_OUT].
 */
class RequestTraceFilter : Filter {
    private val log = LoggerFactory.getLogger(RequestTraceFilter::class.java)

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        val http = request as HttpServletRequest
        trace(TraceStep.FILTER_IN, http)
        try {
            chain.doFilter(request, response)
        } finally {
            // Se registra aunque falle algo más adentro
            trace(TraceStep.FILTER_OUT, http, (response as HttpServletResponse).status)
        }
    }

    private fun trace(
        step: TraceStep,
        request: HttpServletRequest,
        status: Int? = null,
    ) {
        // El paso va como campo propio del JSON para poder buscarlo en el test
        var event = log.atInfo().addKeyValue("step", step.name)
        if (status != null) event = event.addKeyValue("status", status)
        event.log("{} {} {}", step, request.method, request.requestURI)
    }
}
