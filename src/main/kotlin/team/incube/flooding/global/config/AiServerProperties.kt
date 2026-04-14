package team.incube.flooding.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ai.server")
data class AiServerProperties(
    val url: String,
)
