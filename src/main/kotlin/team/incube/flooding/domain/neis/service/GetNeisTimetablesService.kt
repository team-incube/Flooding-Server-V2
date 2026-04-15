package team.incube.flooding.domain.neis.service

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service
import team.incube.flooding.domain.neis.client.NeisTimetableClient
import team.incube.flooding.domain.neis.presentation.data.response.GetNeisTimetablesResponse

@Service
class GetNeisTimetablesService(
    private val neisTimetableClient: NeisTimetableClient,
) {
    fun execute(
        officeCode: String,
        schoolCode: String,
        grade: Int,
        classNumber: Int,
        date: String,
    ): GetNeisTimetablesResponse {
        val response =
            neisTimetableClient.getTimetables(
                officeCode = officeCode,
                schoolCode = schoolCode,
                grade = grade,
                classNumber = classNumber,
                date = date,
            )

        return GetNeisTimetablesResponse(
            date = date,
            grade = grade,
            classNumber = classNumber,
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
                    period =
                        valueOf(periodNode, "PERIO", "period")?.toIntOrNull() ?: index + 1,
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
            targets
                .firstNotNullOfOrNull { node ->
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
                period =
                    valueOf(periodNode, "period", "PERIO")?.toIntOrNull() ?: index + 1,
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
            if (!value.isMissingNode && !value.isNull) {
                return value.asText()
            }
        }
        return null
    }
}
