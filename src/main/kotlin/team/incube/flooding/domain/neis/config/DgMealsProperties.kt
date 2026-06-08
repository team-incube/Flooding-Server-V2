package team.incube.flooding.domain.neis.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "datagsm.neis.meals")
data class DgMealsProperties(
    val baseUrl: String,
    val path: String = "v1/neis/meals",
)
