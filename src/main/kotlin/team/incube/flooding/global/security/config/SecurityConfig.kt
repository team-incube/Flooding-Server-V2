package team.incube.flooding.global.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.global.security.filter.JwtAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/health").permitAll()
                it.requestMatchers("/v2/auth/**").permitAll()
                it.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                // ai
                it.requestMatchers(HttpMethod.POST, "/ai/chat").hasRole(Role.GENERAL_STUDENT.name)
                it.requestMatchers(HttpMethod.POST, "/ai/song").hasRole(Role.GENERAL_STUDENT.name)

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

                it.anyRequest().authenticated()
            }.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}
