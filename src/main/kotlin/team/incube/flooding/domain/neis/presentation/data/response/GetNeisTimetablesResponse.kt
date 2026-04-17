package team.incube.flooding.domain.neis.presentation.data.response

data class GetNeisTimetablesResponse(
    val date: String,
    val grade: Int,
    val classNumber: Int,
    val periods: List<Period>,
) {
    data class Period(
        val period: Int,
        val subject: String,
        val teacher: String?,
        val classroom: String?,
    )
}
