package team.incube.flooding.domain.neis.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import team.incube.flooding.domain.neis.client.dto.GetTimetablesRequest
import team.incube.flooding.domain.neis.config.DgTimetableProperties
import team.themoment.sdk.exception.ExpectedException
import tools.jackson.databind.JsonNode

@Component
class NeisTimetableClient(
    private val dgTimetableProperties: DgTimetableProperties,
    restClientBuilder: RestClient.Builder,
    @Value("\${datagsm.open-api-key}") private val apiKey: String,
) {
    private val restClient =
        restClientBuilder
            .clone()
            .baseUrl(dgTimetableProperties.baseUrl)
            .build()

    fun getTimetables(request: GetTimetablesRequest): JsonNode =
        try {
            restClient
                .get()
                .uri { builder ->
                    builder
                        .path(dgTimetableProperties.path)
                        .queryParam("grade", request.grade)
                        .queryParam("classNum", request.classNumber)
                        .queryParam("date", request.date)
                        .build()
                }.header("X-API-KEY", apiKey)
                .retrieve()
                .body(JsonNode::class.java)
                ?: throw ExpectedException("DG 시간표 응답이 비어 있습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (exception: RestClientResponseException) {
            throw ExpectedException(
                "DG 시간표 호출에 실패했습니다. status=${exception.statusCode.value()}",
                HttpStatus.INTERNAL_SERVER_ERROR,
            )
        } catch (exception: ResourceAccessException) {
            throw ExpectedException(
                "DG 시간표 서버에 연결할 수 없습니다.",
                HttpStatus.INTERNAL_SERVER_ERROR,
            )
        }
}
