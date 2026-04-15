package team.incube.flooding.domain.neis.client

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import team.incube.flooding.domain.neis.config.DgMealsProperties
import team.themoment.sdk.exception.ExpectedException

@Component
class DgMealsClient(
    private val dgMealsProperties: DgMealsProperties,
) {
    private val restClient = RestClient.create()

    @Suppress("UNUSED_PARAMETER")
    fun getMeals(
        officeCode: String,
        schoolCode: String,
        date: String,
    ): JsonNode =
        try {
            restClient
                .get()
                .uri { builder ->
                    builder
                        .scheme("https")
                        .host(dgMealsProperties.baseUrl.removePrefix("https://").removePrefix("http://"))
                        .path(dgMealsProperties.path)
                        .queryParam("date", date)
                        .build()
                }.header("X-API-KEY", dgMealsProperties.apiKey)
                .retrieve()
                .body(JsonNode::class.java)
                ?: throw ExpectedException("DG 급식 응답이 비어 있습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (exception: RestClientResponseException) {
            throw ExpectedException(
                "DG 급식 호출에 실패했습니다. status=${exception.statusCode.value()}",
                HttpStatus.INTERNAL_SERVER_ERROR,
            )
        }
}
