package team.incube.flooding.domain.neis.client.dto

data class GetTimetablesRequest(
    val officeCode: String,
    val schoolCode: String,
    val grade: Int,
    val classNumber: Int,
    val date: String,
)
