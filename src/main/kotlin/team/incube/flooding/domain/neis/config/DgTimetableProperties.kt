package team.incube.flooding.domain.neis.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "datagsm.neis.timetables")
data class DgTimetableProperties(
    val baseUrl: String,
    val path: String = "v1/neis/timetables",
)
