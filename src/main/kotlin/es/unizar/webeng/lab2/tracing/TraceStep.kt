package es.unizar.webeng.lab2.tracing

/**
 * Pasos que se registran cuando una petición viaja por Spring y vuelve al cliente.
 *
 * Están en el orden de ejecución esperado, que comprueba el test de la traza.
 */
enum class TraceStep {
    FILTER_IN,
    ONCE_PER_REQUEST_IN,
    PRE_HANDLE,
    HANDLER,
    BEFORE_BODY_WRITE,
    POST_HANDLE,
    AFTER_COMPLETION,
    ONCE_PER_REQUEST_OUT,
    FILTER_OUT,
}
