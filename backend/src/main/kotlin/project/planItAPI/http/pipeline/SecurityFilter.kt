package project.planItAPI.http.pipeline

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SecurityFilter : HttpFilter() {
    private val tokenCache = ConcurrentHashMap<String, String>()
    private val tokenPattern = "access_token=([^;]*);".toRegex()

    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val uri = request.requestURI
        val isAuthEndpoint = uri.endsWith("/register") || uri.endsWith("/login")

        if (!isAuthEndpoint) {
            val accessToken = request.cookies?.find { it.name == "access_token" }?.value
            if(accessToken == null){
                throwException(response)
                return
            }
            val cachedUserId = tokenCache[accessToken]

            if (cachedUserId == null) {
                throwException(response)
                return
            }else {
                request.setAttribute("userId", cachedUserId)
            }
        }

        chain.doFilter(request, response)

        if (isAuthEndpoint) {
            val cookie = response.getHeader("Set-Cookie")
            val token = extractValue(tokenPattern, cookie)
            val userId = request.getAttribute("userId").toString()

            if (token != null) {
                tokenCache[token] = userId
            }
        }
        return
    }

    fun extractValue(pattern: Regex, value: String): String? {
        val matchResult = pattern.find(value)
        return matchResult?.groupValues?.get(1)
    }

    fun throwException(response: HttpServletResponse){
        response.status = 401
        response.contentType = "application/json"
        response.writer.write("""{"message": "Unauthorized"}""")
    }
}