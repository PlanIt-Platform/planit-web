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

    private val unauthenticatedEndpoints = listOf(
        "/register",
        "/login",
        "/logout",
        "/refresh-token",
        "/about",
        "/categories",
        "/subcategories"
    )

    private val authenticationEndpoints = listOf(
        "/register",
        "/login",
        "/refresh-token",
        "/logout"
    )


    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val uri = request.requestURI

        val accessToken = request.cookies?.find { it.name == "access_token" }?.value

        if(uri.endsWith("/logout") && accessToken != null) tokenCache.remove(accessToken)

        if (!unauthenticatedEndpoints.any{ ae -> uri.endsWith(ae) }) {
            if (accessToken == null) {
                throwException(response)
                return
            }
            val cachedUserId = tokenCache[accessToken]

            if (cachedUserId == null) {
                throwException(response)
                return
            } else {
                request.setAttribute("userId", cachedUserId)
            }
        }

        chain.doFilter(request, response)

        if (authenticationEndpoints.any{ ae -> uri.endsWith(ae) } && (response.status == 201 || response.status == 200)) {
            val userId = request.getAttribute("userId")
            if (uri.endsWith("/refresh-token")) {
                val existingToken = tokenCache.entries.find { it.value == userId }?.key
                if (existingToken != null) {
                    tokenCache.remove(existingToken)
                }
            }
            val cookie = response.getHeader("Set-Cookie")
            val token = extractValue(tokenPattern, cookie)
            if (token != null && userId != null) tokenCache[token] = userId.toString()
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
        response.writer.write("""{"status": 401, "error": "Unauthorized"}""")
    }
}