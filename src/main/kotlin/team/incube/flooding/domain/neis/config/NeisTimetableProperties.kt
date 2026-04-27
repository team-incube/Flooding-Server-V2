package team.incube.flooding.domain.neis.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "neis.openapi.timetable")
data class NeisTimetableProperties(
    val baseUrl: String,
    val apiKey: String,
    val path: String = "hub/hisTimetable",
    val dataType: String = "json",
    val pageSize: Int = 100,
)
