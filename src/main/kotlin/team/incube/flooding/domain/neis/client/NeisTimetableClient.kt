package team.incube.flooding.domain.neis.client

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import team.incube.flooding.domain.neis.client.dto.GetTimetablesRequest
import team.incube.flooding.domain.neis.config.NeisTimetableProperties
import team.themoment.sdk.exception.ExpectedException

@Component
class NeisTimetableClient(
    private val neisTimetableProperties: NeisTimetableProperties,
    restClientBuilder: RestClient.Builder,
) {
    private val restClient = restClientBuilder.build()

    fun getTimetables(request: GetTimetablesRequest): JsonNode =
        try {
            restClient
                .get()
                .uri { builder ->
                    builder
                        .scheme("https")
                        .host(neisTimetableProperties.baseUrl.removePrefix("https://").removePrefix("http://"))
                        .path(neisTimetableProperties.path)
                        .queryParam("KEY", neisTimetableProperties.apiKey)
                        .queryParam("Type", neisTimetableProperties.dataType)
                        .queryParam("pIndex", 1)
                        .queryParam("pSize", neisTimetableProperties.pageSize)
                        .queryParam("ATPT_OFCDC_SC_CODE", request.officeCode)
                        .queryParam("SD_SCHUL_CODE", request.schoolCode)
                        .queryParam("GRADE", request.grade)
                        .queryParam("CLASS_NM", request.classNumber)
                        .queryParam("ALL_TI_YMD", request.date.replace("-", ""))
                        .build()
                }.retrieve()
                .body(JsonNode::class.java)
                ?: throw ExpectedException("NEIS 시간표 응답이 비어 있습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (exception: RestClientResponseException) {
            throw ExpectedException(
                "NEIS 시간표 호출에 실패했습니다. status=${exception.statusCode.value()}",
                HttpStatus.INTERNAL_SERVER_ERROR,
            )
        }
}
