package team.incube.flooding.global.client
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import team.incube.flooding.domain.club.presentation.data.response.GetClubResponse
import java.time.Duration

@Component
class DataGsmProjectClient(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val restClientBuilder: RestClient.Builder,
    @Value("\${datagsm.open-api-key}") private val apiKey: String,
) {
    companion object {
        private const val CACHE_PREFIX = "club:projects:"
        private val CACHE_TTL = Duration.ofMinutes(30)
    }

    private val restClient: RestClient by lazy {
        restClientBuilder
            .baseUrl("https://api.datagsm.com")
            .defaultHeader("X-API-KEY", apiKey)
            .build()
    }

    fun getProjectsByClubId(clubId: Long): List<GetClubResponse.ProjectSummary> {
        val cacheKey = "$CACHE_PREFIX$clubId"
        redisTemplate.opsForValue().get(cacheKey)?.let {
            return objectMapper.readValue(it, object : TypeReference<List<GetClubResponse.ProjectSummary>>() {})
        }
        val projects = fetchFromApi(clubId)
        redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(projects), CACHE_TTL)
        return projects
    }

    private fun fetchFromApi(clubId: Long): List<GetClubResponse.ProjectSummary> =
        runCatching {
            restClient
                .get()
                .uri("/v1/projects?clubId=$clubId")
                .retrieve()
                .body(DataGsmResponse::class.java)
                ?.data
                ?.projects
                ?.map { it.toSummary() }
        }.getOrNull() ?: emptyList()

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
