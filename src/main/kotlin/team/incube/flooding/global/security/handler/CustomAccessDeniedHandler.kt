package team.incube.flooding.global.security.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler : AccessDeniedHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        ex: AccessDeniedException,
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        log.warn(
            "403 Forbidden: method={}, uri={}, user={}, authorities={}, message={}",
            request.method,
            request.requestURI,
            authentication?.name ?: "anonymous",
            authentication?.authorities ?: "none",
            ex.message,
        )
        response.sendError(HttpServletResponse.SC_FORBIDDEN)
    }
}
