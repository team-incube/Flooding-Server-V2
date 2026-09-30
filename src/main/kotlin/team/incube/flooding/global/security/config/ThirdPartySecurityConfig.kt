package team.incube.flooding.global.security.config

import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfigurationSource

/**
 * `/third-party` 하위 모든 경로에 공통으로 적용되는 DataGSM 서드파티 스코프 보안 설정.
 * 서드파티에 공개할 API는 이 경로 아래에 두기만 하면 별도 설정 없이 스코프 검사가 적용된다.
 *
 * 서드파티는 DataGSM OAuth로 발급받은 RS256 액세스 토큰을 `Authorization: Bearer` 헤더로 전달하며,
 * 토큰은 DataGSM JWK Set으로 서명 검증 후 `scopes` 클레임에 Flooding 스코프가 포함되어 있어야 한다.
 * 자체 JWT를 사용하는 기본 체인([SecurityConfig])보다 먼저 매칭되도록 우선순위를 높게 둔다.
 */
@Configuration
class ThirdPartySecurityConfig(
    private val corsConfigurationSource: CorsConfigurationSource,
) {
    @Bean
    @Order(1)
    fun thirdPartySecurityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .securityMatcher("/third-party/**")
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests {
                it
                    .dispatcherTypeMatchers(
                        DispatcherType.ASYNC,
                        DispatcherType.ERROR,
                        DispatcherType.FORWARD,
                    ).permitAll()
                it.anyRequest().hasAuthority("$SCOPE_AUTHORITY_PREFIX$FLOODING_SCOPE")
            }.oauth2ResourceServer {
                it.jwt { jwt ->
                    jwt
                        .decoder(thirdPartyJwtDecoder())
                        .jwtAuthenticationConverter(thirdPartyJwtAuthenticationConverter())
                }
            }.build()

    private fun thirdPartyJwtDecoder(): JwtDecoder = NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build()

    private fun thirdPartyJwtAuthenticationConverter(): JwtAuthenticationConverter {
        val authoritiesConverter =
            JwtGrantedAuthoritiesConverter().apply {
                setAuthoritiesClaimName(SCOPES_CLAIM)
                setAuthorityPrefix(SCOPE_AUTHORITY_PREFIX)
            }
        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(authoritiesConverter)
        }
    }

    companion object {
        const val FLOODING_SCOPE = "55fc9c58-377b-4e53-bcf3-9fc38aac912e:flooding"
        private const val JWK_SET_URI = "https://oauth.authorization.datagsm.kr/v1/oauth/jwks"
        private const val SCOPES_CLAIM = "scopes"
        private const val SCOPE_AUTHORITY_PREFIX = "SCOPE_"
    }
}
