package team.incube.flooding.global.auth.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.sdk.oauth.DataGsmClient

@Configuration
class OAuthConfig(
    @Value($$"${oauth.client-id}") private val clientId: String,
    @Value("\${oauth.client-secret}") private val clientSecret: String
) {
    @Bean
    fun dataGsmClient(): DataGsmClient =
        DataGsmClient.builder(clientSecret).build()
}