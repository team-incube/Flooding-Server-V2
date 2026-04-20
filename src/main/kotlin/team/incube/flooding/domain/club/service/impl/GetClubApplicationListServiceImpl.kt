package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.presentation.data.response.GetClubApplicationListResponse
import team.incube.flooding.domain.club.repository.ClubFormAnswerRepository
import team.incube.flooding.domain.club.repository.ClubFormRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.club.repository.ClubRepository
import team.incube.flooding.domain.club.service.GetClubApplicationListService
import team.incube.flooding.domain.user.entity.Role
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class GetClubApplicationListServiceImpl(
    private val clubRepository: ClubRepository,
    private val clubFormRepository: ClubFormRepository,
    private val submissionRepository: ClubFormSubmissionRepository,
    private val answerRepository: ClubFormAnswerRepository,
    private val currentUserProvider: CurrentUserProvider,
) : GetClubApplicationListService {
    @Transactional(readOnly = true)
    override fun execute(clubId: Long): GetClubApplicationListResponse {
        val currentUser = currentUserProvider.getCurrentUser()
        val club =
            clubRepository.findByIdWithLeader(clubId)
                ?: throw ExpectedException("존재하지 않는 동아리입니다.", HttpStatus.NOT_FOUND)

        val isLeader = club.leader?.id == currentUser.id
        val isPrivileged = currentUser.role in listOf(Role.ADMIN, Role.STUDENT_COUNCIL)
        if (!isLeader && !isPrivileged) {
            throw ExpectedException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        val form =
            clubFormRepository.findByClubIdAndIsActiveTrue(clubId)
                ?: throw ExpectedException("생성된 폼이 없습니다.", HttpStatus.NOT_FOUND)

        val submissions = submissionRepository.findAllByFormIdWithUser(form.id)
        if (submissions.isEmpty()) return GetClubApplicationListResponse(emptyList())

        val submissionIds = submissions.map { it.id }
        val answersBySubmission =
            answerRepository
                .findAllBySubmissionIdIn(submissionIds)
                .groupBy { it.submission.id }

        val applications =
            submissions.map { submission ->
                val answers =
                    answersBySubmission[submission.id].orEmpty().map { answer ->
                        GetClubApplicationListResponse.AnswerInfo(
                            fieldId = answer.field.id,
                            label = answer.field.label,
                            value = answer.value,
                        )
                    }
                GetClubApplicationListResponse.ApplicationSummary(
                    submissionId = submission.id,
                    applicant =
                        GetClubApplicationListResponse.ApplicantInfo(
                            id = submission.user.id,
                            name = submission.user.name,
                            studentNumber = submission.user.studentNumber,
                        ),
                    submittedAt = submission.submittedAt,
                    answers = answers,
                )
            }
        return GetClubApplicationListResponse(applications)
    }
}
