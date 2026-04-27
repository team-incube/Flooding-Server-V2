package team.incube.flooding.global.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class DataGsmClubClient(
    @Value("\${datagsm.base-url}") private val baseUrl: String,
    @Value("\${datagsm.open-api-key}") private val apiKey: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val restClient: RestClient by lazy {
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .defaultHeader("X-API-KEY", apiKey)
            .build()
    }

    fun createClub(request: ClubReqDto): Long? =
        runCatching {
            restClient
                .post()
                .uri("/v1/clubs")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ClubResponse::class.java)
                ?.data
                ?.id
        }.onFailure { log.error("DataGSM 동아리 생성 실패: name=${request.name}", it) }
            .getOrNull()

    fun getStudentIdByEmail(email: String): Long? =
        runCatching {
            restClient
                .get()
                .uri { it.path("/v1/students").queryParam("email", email).build() }
                .retrieve()
                .body(StudentListResponse::class.java)
                ?.data
                ?.students
                ?.firstOrNull()
                ?.id
        }.getOrNull()

    data class ClubReqDto(
        val name: String,
        val type: String,
        val leaderId: Long?,
        val participantIds: List<Long>,
        val foundedYear: Int,
        val status: String,
    )

    data class ClubResponse(
        val data: ClubData,
    ) {
        data class ClubData(
            val id: Long,
        )
    }

    data class StudentListResponse(
        val data: StudentListData,
    ) {
        data class StudentListData(
            val students: List<StudentData>,
        )

        data class StudentData(
            val id: Long,
            val email: String,
        )
    }
}
