package team.incube.flooding.domain.neis.client.dto

data class GetTimetablesRequest(
    val grade: Int,
    val classNumber: Int,
    val date: String,
)
