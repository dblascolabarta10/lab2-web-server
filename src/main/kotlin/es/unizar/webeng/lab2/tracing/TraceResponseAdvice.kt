package es.unizar.webeng.lab2.tracing

import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

/** Cabecera que añade el advice con el método que atendió la petición. */
const val HANDLER_HEADER = "X-Handler"

/**
 * Advice de Spring MVC: actúa justo antes de convertir a JSON lo que devuelve
 * el controlador. Registra [TraceStep.BEFORE_BODY_WRITE] y decora la respuesta
 * con la cabecera [HANDLER_HEADER], sin cambiar el cuerpo.
 */
@ControllerAdvice
class TraceResponseAdvice : ResponseBodyAdvice<Any> {
    private val log = LoggerFactory.getLogger(TraceResponseAdvice::class.java)

    // Se aplica a todas las respuestas con cuerpo (las de @RestController)
    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        log.traceStep(TraceStep.BEFORE_BODY_WRITE, "${request.method} ${request.uri.path}")
        response.headers.add(HANDLER_HEADER, "${returnType.containingClass.simpleName}.${returnType.method?.name}")
        // Se devuelve el cuerpo tal cual para no romper el JSON de /time
        return body
    }
}
