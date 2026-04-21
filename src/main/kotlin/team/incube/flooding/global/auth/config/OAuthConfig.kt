package team.incube.flooding.global.auth.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient

@Configuration
class OAuthConfig(
    @Value($$"${oauth.client-id}") private val clientId: String,
    @Value($$"${oauth.client-secret}") private val clientSecret: String,
    @Value($$"${oauth.redirect-uri}") val redirectUri: String,
) {
    @Bean
    fun dataGsmClient(): DataGsmOAuthClient = DataGsmOAuthClient.builder(clientId, clientSecret).build()
}
