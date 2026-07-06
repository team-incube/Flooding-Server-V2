package team.incube.flooding.domain.neis.presentation.data.request

data class GetNeisTimetablesRequest(
    val grade: Int,
    val classNumber: Int,
    val date: String,
)
