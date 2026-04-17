package team.incube.flooding.domain.neis.service.impl

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service
import team.incube.flooding.domain.neis.client.NeisTimetableClient
import team.incube.flooding.domain.neis.client.dto.GetTimetablesRequest
import team.incube.flooding.domain.neis.presentation.data.request.GetNeisTimetablesRequest
import team.incube.flooding.domain.neis.presentation.data.response.GetNeisTimetablesResponse
import team.incube.flooding.domain.neis.service.GetNeisTimetablesService

@Service
class GetNeisTimetablesServiceImpl(
    private val neisTimetableClient: NeisTimetableClient,
) : GetNeisTimetablesService {
    override fun execute(request: GetNeisTimetablesRequest): GetNeisTimetablesResponse {
        val response =
            neisTimetableClient.getTimetables(
                GetTimetablesRequest(
                    officeCode = request.officeCode,
                    schoolCode = request.schoolCode,
                    grade = request.grade,
                    classNumber = request.classNumber,
                    date = request.date,
                ),
            )
        return GetNeisTimetablesResponse(
            date = request.date,
            grade = request.grade,
            classNumber = request.classNumber,
            periods = extractPeriods(response),
        )
    }

    private fun extractPeriods(response: JsonNode): List<GetNeisTimetablesResponse.Period> {
        val neisRows =
            response
                .path("hisTimetable")
                .find { node -> node.path("row").isArray }
                ?.path("row")
        if (neisRows != null && neisRows.isArray) {
            return neisRows.mapIndexed { index, periodNode ->
                GetNeisTimetablesResponse.Period(
                    period = valueOf(periodNode, "PERIO", "period")?.toIntOrNull() ?: index + 1,
                    subject = valueOf(periodNode, "ITRT_CNTNT", "subject") ?: "미정",
                    teacher = valueOf(periodNode, "TEACHER_NM", "teacher"),
                    classroom = valueOf(periodNode, "CLRM_NM", "CLASSROOM", "classroom"),
                )
            }
        }

        val targets =
            listOf(
                response.path("data"),
                response.path("timetables"),
                response,
            )

        val periodNodes =
            targets.firstNotNullOfOrNull { node ->
                when {
                    node.isArray -> node
                    node.path("periods").isArray -> node.path("periods")
                    node.path("timetables").isArray -> node.path("timetables")
                    node.path("data").isArray -> node.path("data")
                    else -> null
                }
            } ?: return emptyList()

        return periodNodes.mapIndexed { index, periodNode ->
            GetNeisTimetablesResponse.Period(
                period = valueOf(periodNode, "period", "PERIO")?.toIntOrNull() ?: index + 1,
                subject = valueOf(periodNode, "subject", "ITRT_CNTNT") ?: "미정",
                teacher = valueOf(periodNode, "teacher", "TEACHER_NM"),
                classroom = valueOf(periodNode, "classroom", "CLASSROOM", "CLRM_NM"),
            )
        }
    }

    private fun valueOf(
        node: JsonNode,
        vararg keys: String,
    ): String? {
        keys.forEach { key ->
            val value = node.path(key)
            if (!value.isMissingNode && !value.isNull) return value.asText()
        }
        return null
    }
}
