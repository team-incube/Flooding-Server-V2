package team.incube.flooding.global.security.config

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
                it.requestMatchers("/auth/signin", "/auth/reissue").permitAll()
                it.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                // ai
                it.requestMatchers(HttpMethod.POST, "/ai/chat").hasRole(Role.GENERAL_STUDENT.name)
                it.requestMatchers(HttpMethod.POST, "/ai/song").hasRole(Role.GENERAL_STUDENT.name)

                // club
                it
                    .requestMatchers(
                        HttpMethod.PATCH,
                        "/clubs/*/approval",
                    ).hasAnyRole(Role.ADMIN.name, Role.STUDENT_COUNCIL.name)

                // study
                it.requestMatchers(HttpMethod.POST, "/dormitory/studies").hasRole(Role.GENERAL_STUDENT.name)
                it.requestMatchers(HttpMethod.DELETE, "/dormitory/studies").hasRole(Role.GENERAL_STUDENT.name)
                it
                    .requestMatchers(
                        HttpMethod.POST,
                        "/dormitory/studies/ban/**",
                    ).hasAnyRole(Role.DORMITORY_MANAGER.name, Role.ADMIN.name)

                // massage
                it.requestMatchers(HttpMethod.POST, "/dormitory/massages").hasRole(Role.GENERAL_STUDENT.name)
                it.requestMatchers(HttpMethod.DELETE, "/dormitory/massages").hasRole(Role.GENERAL_STUDENT.name)

                // music
                it
                    .requestMatchers(
                        HttpMethod.GET,
                        "/dormitory/music",
                    ).hasAnyRole(Role.GENERAL_STUDENT.name, Role.DORMITORY_MANAGER.name, Role.ADMIN.name)
                it.requestMatchers(HttpMethod.POST, "/dormitory/music").hasRole(Role.GENERAL_STUDENT.name)
                it
                    .requestMatchers(
                        HttpMethod.DELETE,
                        "/dormitory/music/{musicId}",
                    ).hasAnyRole(Role.GENERAL_STUDENT.name, Role.DORMITORY_MANAGER.name, Role.ADMIN.name)
                it
                    .requestMatchers(
                        HttpMethod.POST,
                        "/dormitory/music/{musicId}/like",
                    ).hasRole(Role.GENERAL_STUDENT.name)
                it
                    .requestMatchers(
                        HttpMethod.DELETE,
                        "/dormitory/music/{musicId}/like",
                    ).hasRole(Role.GENERAL_STUDENT.name)

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
            }.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}
