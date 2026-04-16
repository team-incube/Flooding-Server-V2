package team.incube.flooding.domain.club.presentation.data.response

data class GetClubApplicationResponse(
    val application: List<ApplicationResponse>,
) {
    data class ApplicationResponse(
        val answers: List<AnswerResponse>,
    )

    data class AnswerResponse(
        val questionId: Long,
        val value: String,
    )
}
