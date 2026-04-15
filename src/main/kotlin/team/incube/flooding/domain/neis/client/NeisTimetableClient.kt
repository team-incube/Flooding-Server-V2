package team.incube.flooding.domain.neis.client

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import team.incube.flooding.domain.neis.config.NeisTimetableProperties
import team.themoment.sdk.exception.ExpectedException

@Component
class NeisTimetableClient(
    private val neisTimetableProperties: NeisTimetableProperties,
) {
    private val restClient = RestClient.create()

    fun getTimetables(
        officeCode: String,
        schoolCode: String,
        grade: Int,
        classNumber: Int,
        date: String,
    ): JsonNode =
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
                        .queryParam("ATPT_OFCDC_SC_CODE", officeCode)
                        .queryParam("SD_SCHUL_CODE", schoolCode)
                        .queryParam("GRADE", grade)
                        .queryParam("CLASS_NM", classNumber)
                        .queryParam("ALL_TI_YMD", date.replace("-", ""))
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

