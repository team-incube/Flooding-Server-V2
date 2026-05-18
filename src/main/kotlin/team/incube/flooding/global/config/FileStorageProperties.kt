package team.incube.flooding.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "file")
data class FileStorageProperties(
    val bucket: String,
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val publicBaseUrl: String,
    val region: String = "auto",
)
