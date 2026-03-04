package team.incube.flooding.global.security.config

import io.swagger.v3.oas.models.PathItem
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import team.incube.flooding.global.security.filter.JwtAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/v2/auth/**").permitAll()
                it.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()

                //자습
                it.requestMatchers(HttpMethod.POST, "/study").hasRole("GENERAL_STUDENT")
                it.requestMatchers(HttpMethod.DELETE, "/study").hasRole("GENERAL_STUDENT")
                it.requestMatchers(HttpMethod.POST, "/study/ban/**").hasAnyRole("DORMITORY_MANAGER", "ADMIN")
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}