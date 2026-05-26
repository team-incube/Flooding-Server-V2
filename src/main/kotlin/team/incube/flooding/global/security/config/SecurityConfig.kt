package team.incube.flooding.global.security.config

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.global.security.filter.JwtAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config =
            CorsConfiguration().apply {
                allowedOriginPatterns = listOf("*")
                allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                allowedHeaders = listOf("*")
                allowCredentials = true
            }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/**").permitAll()
                it.requestMatchers("/error").permitAll()
                it.requestMatchers("/auth/signin", "/auth/reissue").permitAll()
                it.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                // ai
                it.requestMatchers(HttpMethod.POST, "/ai/chat").authenticated()
                it.requestMatchers(HttpMethod.POST, "/ai/song").authenticated()

                // club
                it
                    .requestMatchers(HttpMethod.GET, "/clubs/opening/requests")
                    .hasAnyRole(Role.ADMIN.name, Role.STUDENT_COUNCIL.name)
                it
                    .requestMatchers(HttpMethod.PATCH, "/clubs/*/approval")
                    .hasAnyRole(Role.ADMIN.name, Role.STUDENT_COUNCIL.name)
                it
                    .requestMatchers(HttpMethod.PUT, "/clubs/*")
                    .hasAnyRole(Role.ADMIN.name, Role.GENERAL_STUDENT.name, Role.STUDENT_COUNCIL.name)
                it
                    .requestMatchers(HttpMethod.POST, "/clubs/representative-image")
                    .hasAnyRole(Role.ADMIN.name, Role.GENERAL_STUDENT.name, Role.STUDENT_COUNCIL.name)
                it
                    .requestMatchers(
                        HttpMethod.POST,
                        "/clubs/*/autonomous/applications",
                        "/clubs/*/applications",
                    ).hasAnyRole(Role.GENERAL_STUDENT.name, Role.STUDENT_COUNCIL.name, Role.ADMIN.name)

                // study
                it.requestMatchers(HttpMethod.POST, "/dormitory/studies").authenticated()
                it.requestMatchers(HttpMethod.DELETE, "/dormitory/studies").authenticated()
                it
                    .requestMatchers(
                        HttpMethod.POST,
                        "/dormitory/studies/ban/**",
                    ).hasAnyRole(Role.DORMITORY_MANAGER.name, Role.ADMIN.name)
                it
                    .requestMatchers(
                        HttpMethod.DELETE,
                        "/dormitory/studies/ban/**",
                    ).hasAnyRole(Role.DORMITORY_MANAGER.name, Role.ADMIN.name)
                it
                    .requestMatchers(HttpMethod.GET, "/dormitory/studies/attendance")
                    .hasAnyRole(Role.DORMITORY_MANAGER.name, Role.ADMIN.name)
                it
                    .requestMatchers(HttpMethod.POST, "/dormitory/studies/attendance/*")
                    .hasAnyRole(Role.DORMITORY_MANAGER.name, Role.ADMIN.name)
                it
                    .requestMatchers(HttpMethod.DELETE, "/dormitory/studies/attendance/*")
                    .hasAnyRole(Role.DORMITORY_MANAGER.name, Role.ADMIN.name)

                // music
                it.requestMatchers(HttpMethod.GET, "/dormitory/music").authenticated()
                it.requestMatchers(HttpMethod.POST, "/dormitory/music").authenticated()
                it.requestMatchers(HttpMethod.DELETE, "/dormitory/music/*").authenticated()
                it.requestMatchers(HttpMethod.POST, "/dormitory/music/*/like").authenticated()
                it.requestMatchers(HttpMethod.DELETE, "/dormitory/music/*/like").authenticated()

                // massage
                it.requestMatchers(HttpMethod.GET, "/dormitory/massages").authenticated()
                it.requestMatchers(HttpMethod.POST, "/dormitory/massages").authenticated()
                it.requestMatchers(HttpMethod.DELETE, "/dormitory/massages").authenticated()

                // penalty
                it
                    .requestMatchers(HttpMethod.GET, "/dormitory/penalties")
                    .hasAnyRole(Role.DORMITORY_MANAGER.name, Role.ADMIN.name)
                it
                    .requestMatchers(HttpMethod.PUT, "/dormitory/penalties/*")
                    .hasAnyRole(Role.DORMITORY_MANAGER.name, Role.ADMIN.name)

                // cleaning-zones
                it
                    .requestMatchers(HttpMethod.POST, "/dormitory/cleaning-zones")
                    .hasAnyRole(Role.DORMITORY_MANAGER.name, Role.ADMIN.name)
                it
                    .requestMatchers(HttpMethod.PATCH, "/dormitory/cleaning-zones/*/members")
                    .hasAnyRole(Role.DORMITORY_MANAGER.name, Role.ADMIN.name)

                it.anyRequest().authenticated()
            }.exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                }
            }.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}
