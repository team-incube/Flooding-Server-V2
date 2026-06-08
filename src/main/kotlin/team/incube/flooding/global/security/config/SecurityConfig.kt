package team.incube.flooding.global.security.config

import jakarta.servlet.DispatcherType
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
import team.incube.flooding.global.security.handler.CustomAccessDeniedHandler

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
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
                // SSE 등 비동기 요청의 ASYNC/ERROR/FORWARD 재디스패치는 인가를 재평가하지 않도록 permit.
                // STATELESS + ThreadLocal 환경에서 재디스패치 시 SecurityContext가 비어 Access Denied가
                // 발생하고, 이미 commit된 text/event-stream 응답이 끊겨 재연결 폭주가 생기는 것을 방지한다.
                // (Spring Security 7에서 제거된 shouldFilterAllDispatcherTypes(false) 대체)
                it
                    .dispatcherTypeMatchers(
                        DispatcherType.ASYNC,
                        DispatcherType.ERROR,
                        DispatcherType.FORWARD,
                    ).permitAll()
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
                it.requestMatchers(HttpMethod.GET, "/dormitory/music/subscribe").authenticated()
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

                // user
                it
                    .requestMatchers(HttpMethod.PATCH, "/users/*/role")
                    .hasAnyRole(Role.DORMITORY_MANAGER.name, Role.ADMIN.name)

                it.anyRequest().authenticated()
            }.exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                }
                it.accessDeniedHandler(customAccessDeniedHandler)
            }.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}
