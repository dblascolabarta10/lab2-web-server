package es.unizar.webeng.lab2.tracing

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

/** Registra los componentes de la traza de peticiones. */
@Configuration
class TracingConfig {
    @Bean
    fun requestTraceFilter(): FilterRegistrationBean<RequestTraceFilter> =
        FilterRegistrationBean(RequestTraceFilter()).apply {
            addUrlPatterns("/*")
            // Casi el primero de la cadena, pero deja hueco por delante
            order = Ordered.HIGHEST_PRECEDENCE + 10
        }
}
