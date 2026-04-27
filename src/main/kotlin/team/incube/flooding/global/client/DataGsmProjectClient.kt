package team.incube.flooding.global.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import team.incube.flooding.domain.club.presentation.data.response.GetClubResponse
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper

@Component
class DataGsmProjectClient(
    private val redisTemplate: RedisTemplate<String, String>,
    private val jsonMapper: JsonMapper,
    private val restClientBuilder: RestClient.Builder,
    @Value("\${datagsm.open-api-key}") private val apiKey: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val CACHE_PREFIX = "club:projects:"
    }

    private val restClient: RestClient by lazy {
        restClientBuilder
            .baseUrl("https://openapi.datagsm.kr")
            .defaultHeader("X-API-KEY", apiKey)
            .build()
    }

    suspend fun getProjectsByClubId(clubId: Long): List<GetClubResponse.ProjectSummary> =
        withContext(Dispatchers.IO) {
            runCatching {
                val cacheKey = "$CACHE_PREFIX$clubId"
                val cached = redisTemplate.opsForValue().get(cacheKey)
                if (cached != null) {
                    jsonMapper.readValue(cached, object : TypeReference<List<GetClubResponse.ProjectSummary>>() {})
                } else {
                    warmCache(clubId) ?: emptyList()
                }
            }.getOrElse { emptyList() }
        }

    fun warmCache(clubId: Long): List<GetClubResponse.ProjectSummary>? {
        val projects =
            fetchFromApi(clubId) ?: run {
                log.warn("DataGSM API 응답 없음, 기존 캐시 유지: clubId=$clubId")
                return null
            }
        val cacheKey = "$CACHE_PREFIX$clubId"
        redisTemplate.opsForValue().set(cacheKey, jsonMapper.writeValueAsString(projects))
        return projects
    }

    private fun fetchFromApi(clubId: Long): List<GetClubResponse.ProjectSummary>? =
        runCatching {
            restClient
                .get()
                .uri("/v1/projects?clubId=$clubId")
                .retrieve()
                .body(DataGsmResponse::class.java)
                ?.data
                ?.projects
                ?.map { it.toSummary() }
        }.onFailure { log.error("DataGSM 프로젝트 조회 실패: clubId=$clubId", it) }
            .getOrNull()

    data class DataGsmResponse(
        val data: ProjectData,
    ) {
        data class ProjectData(
            val projects: List<Project>,
        )

        data class Project(
            val id: Long,
            val name: String,
            val description: String,
            val participants: List<Participant>,
        ) {
            fun toSummary() =
                GetClubResponse.ProjectSummary(
                    id = id,
                    name = name,
                    description = description,
                    participants =
                        participants.map {
                            GetClubResponse.ParticipantSummary(
                                id = it.id,
                                name = it.name,
                                studentNumber = it.studentNumber,
                                sex = it.sex,
                                specialty = it.major,
                            )
                        },
                )
        }

        data class Participant(
            val id: Long,
            val name: String,
            val studentNumber: Int?,
            val major: String?,
            val sex: String,
        )
    }
}
