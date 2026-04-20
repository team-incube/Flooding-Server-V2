package team.incube.flooding.domain.club.presentation.data.response

import java.time.LocalDateTime

data class GetClubApplicationListResponse(
    val applications: List<ApplicationSummary>,
) {
    data class ApplicationSummary(
        val submissionId: Long,
        val applicant: ApplicantInfo,
        val submittedAt: LocalDateTime,
        val answers: List<AnswerInfo>,
    )

    data class ApplicantInfo(
        val id: Long,
        val name: String,
        val studentNumber: Int,
    )

    data class AnswerInfo(
        val fieldId: Long,
        val label: String,
        val value: String?,
    )
}
