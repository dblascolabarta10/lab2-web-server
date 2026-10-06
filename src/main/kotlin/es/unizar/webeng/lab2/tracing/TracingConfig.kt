package es.unizar.webeng.lab2.tracing

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/** Registra los componentes de la traza de peticiones. */
@Configuration
class TracingConfig : WebMvcConfigurer {
    @Bean
    fun requestTraceFilter(): FilterRegistrationBean<RequestTraceFilter> =
        FilterRegistrationBean(RequestTraceFilter()).apply {
            addUrlPatterns("/*")
            // Casi el primero de la cadena, pero deja hueco por delante
            order = Ordered.HIGHEST_PRECEDENCE + 10
        }

    @Bean
    fun onceTraceFilter(): FilterRegistrationBean<OncePerRequestTraceFilter> =
        FilterRegistrationBean(OncePerRequestTraceFilter()).apply {
            addUrlPatterns("/*")
            // Justo después del filtro anterior, que es quien crea el id
            order = Ordered.HIGHEST_PRECEDENCE + 20
        }

    // Los interceptores no se registran como filtros, sino en Spring MVC
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(TraceInterceptor())
    }
}
