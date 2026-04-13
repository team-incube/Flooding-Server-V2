package team.incube.flooding.domain.club.presentation.data.request

data class CreateClubApplicationRequest(
    val answers: List<CreateClubApplicationAnswerRequest>,
)

data class CreateClubApplicationAnswerRequest(
    val fieldId: Long,
    val value: String?,
)
