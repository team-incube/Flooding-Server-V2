package team.incube.flooding.domain.neis.client

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import team.incube.flooding.domain.neis.client.dto.GetMealsRequest
import team.incube.flooding.domain.neis.config.DgMealsProperties
import team.themoment.sdk.exception.ExpectedException
import tools.jackson.databind.JsonNode

@Component
class DgMealsClient(
    private val dgMealsProperties: DgMealsProperties,
    restClientBuilder: RestClient.Builder,
) {
    private val restClient =
        restClientBuilder
            .clone()
            .baseUrl(dgMealsProperties.baseUrl)
            .build()

    fun getMeals(request: GetMealsRequest): JsonNode =
        try {
            restClient
                .get()
                .uri { builder ->
                    builder
                        .path(dgMealsProperties.path)
                        .queryParam("date", request.date)
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
        } catch (exception: ResourceAccessException) {
            throw ExpectedException(
                "DG 급식 서버에 연결할 수 없습니다.",
                HttpStatus.INTERNAL_SERVER_ERROR,
            )
        }
}
