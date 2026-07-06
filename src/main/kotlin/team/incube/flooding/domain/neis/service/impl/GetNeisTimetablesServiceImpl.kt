package team.incube.flooding.domain.neis.service.impl

import org.springframework.stereotype.Service
import team.incube.flooding.domain.neis.client.NeisTimetableClient
import team.incube.flooding.domain.neis.client.dto.GetTimetablesRequest
import team.incube.flooding.domain.neis.presentation.data.request.GetNeisTimetablesRequest
import team.incube.flooding.domain.neis.presentation.data.response.GetNeisTimetablesResponse
import team.incube.flooding.domain.neis.service.GetNeisTimetablesService
import tools.jackson.databind.JsonNode

@Service
class GetNeisTimetablesServiceImpl(
    private val neisTimetableClient: NeisTimetableClient,
) : GetNeisTimetablesService {
    override fun execute(request: GetNeisTimetablesRequest): GetNeisTimetablesResponse {
        val response =
            neisTimetableClient.getTimetables(
                GetTimetablesRequest(
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

        val rawNodes: List<JsonNode> =
            if (neisRows != null && neisRows.isArray) {
                neisRows.map { it }
            } else {
                val targets = listOf(response.path("data"), response.path("timetables"), response)
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
                periodNodes.map { it }
            }

        val periodMap = mutableMapOf<Int, GetNeisTimetablesResponse.Period>()

        rawNodes.forEachIndexed { idx, periodNode ->
            val periodNumbers = parsePeriodNumbers(periodNode, idx)

            val nodePeriod =
                GetNeisTimetablesResponse.Period(
                    period = periodNumbers.firstOrNull() ?: (idx + 1),
                    subject = valueOf(periodNode, "ITRT_CNTNT", "subject") ?: "미정",
                    teacher = valueOf(periodNode, "TEACHER_NM", "teacher"),
                    classroom = valueOf(periodNode, "CLRM_NM", "CLASSROOM", "classroom", "classroomName"),
                )

            periodNumbers.forEach { periodNum ->
                periodMap[periodNum] = nodePeriod.copy(period = periodNum)
            }
        }

        return periodMap.toSortedMap().values.toList()
    }

    @Suppress("DEPRECATION")
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

    private fun parsePeriodNumbers(
        node: JsonNode,
        idx: Int,
    ): List<Int> {
        val raw = valueOf(node, "PERIO", "period")?.trim() ?: return listOf(idx + 1)
        var s = raw.replace("\\s".toRegex(), "")
        s = s.replace("[–—·]".toRegex(), "-")
        if (s.contains(",")) {
            val parts = s.split(",")
            val expanded =
                parts.flatMap { part ->
                    val p = part.trim()
                    if (p.contains("-")) {
                        val dashIndex = p.indexOf('-')
                        if (dashIndex > 0) {
                            val left = p.substring(0, dashIndex)
                            val right = p.substring(dashIndex + 1)
                            val start = left.toIntOrNull()
                            val end = right.toIntOrNull()
                            if (start != null && end != null && end >= start) return@flatMap (start..end).toList()
                        }
                        listOfNotNull(p.toIntOrNull())
                    } else {
                        listOfNotNull(p.toIntOrNull())
                    }
                }
            return expanded.distinct().filter { it > 0 }
        }

        if (s.contains("-")) {
            val dashIndex = s.indexOf('-')
            if (dashIndex > 0) {
                val left = s.substring(0, dashIndex)
                val right = s.substring(dashIndex + 1)
                val start = left.toIntOrNull()
                val end = right.toIntOrNull()
                if (start != null && end != null && end >= start) {
                    return (start..end).toList()
                }
            }
        }
        val single = s.toIntOrNull()
        if (single != null && single > 0) return listOf(single)
        return listOf(idx + 1)
    }
}
