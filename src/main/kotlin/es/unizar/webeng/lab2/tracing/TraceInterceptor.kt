package es.unizar.webeng.lab2.tracing

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

/**
 * Interceptor de Spring MVC: actúa alrededor del controlador, ya con el
 * handler elegido. Registra [TraceStep.PRE_HANDLE], [TraceStep.POST_HANDLE]
 * y [TraceStep.AFTER_COMPLETION].
 */
class TraceInterceptor : HandlerInterceptor {
    private val log = LoggerFactory.getLogger(TraceInterceptor::class.java)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        log.traceStep(TraceStep.PRE_HANDLE, request)
        // Con false la petición no llegaría al controlador
        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?,
    ) {
        // Solo se llama si el controlador termina sin excepción
        log.traceStep(TraceStep.POST_HANDLE, request, response.status)
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        // Se llama siempre, haya ido bien o mal
        log.traceStep(TraceStep.AFTER_COMPLETION, request, response.status)
    }
}
