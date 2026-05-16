package team.incube.flooding.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "file")
data class FileStorageProperties(
    val uploadDir: String,
    val baseUrl: String,
)
