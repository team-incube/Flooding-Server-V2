package team.incube.flooding.global.client

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class YoutubeApiClientConfig {
    @Bean
    fun youtubeApiKeyInterceptor(
        @Value("\${youtube.api-key}") apiKey: String,
    ): RequestInterceptor = RequestInterceptor { template -> template.query("key", apiKey) }
}
