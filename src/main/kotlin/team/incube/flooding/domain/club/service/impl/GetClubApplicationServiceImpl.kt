package team.incube.flooding.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.club.presentation.data.response.GetClubApplicationResponse
import team.incube.flooding.domain.club.repository.ClubFormAnswerRepository
import team.incube.flooding.domain.club.repository.ClubFormSubmissionRepository
import team.incube.flooding.domain.club.service.GetClubApplicationService
import team.incube.flooding.domain.user.entity.Role // 권한 Enum 임포트
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException // 예외 클래스 임포트

@Service
class GetClubApplicationServiceImpl(
    private val clubFormSubmissionRepository: ClubFormSubmissionRepository,
    private val clubFormAnswerRepository: ClubFormAnswerRepository,
    private val currentUserProvider: CurrentUserProvider,
) : GetClubApplicationService {
    @Transactional(readOnly = true)
    override fun execute(clubId: Long): GetClubApplicationResponse {
        val user = currentUserProvider.getCurrentUser()

        if (user.role != Role.GENERAL_STUDENT) {
            throw ExpectedException("학생 권한이 필요합니다.", HttpStatus.FORBIDDEN)
        }

        val submissions = clubFormSubmissionRepository.findAllByFormClubId(clubId)

        val allAnswers = clubFormAnswerRepository.findAllBySubmissionIn(submissions)

        val answerMap = allAnswers.groupBy { it.submission.id }

        return GetClubApplicationResponse(
            application =
                submissions.map { submission ->
                    GetClubApplicationResponse.ApplicationResponse(
                        answers =
                            answerMap[submission.id]?.map { answer ->
                                GetClubApplicationResponse.AnswerResponse(
                                    questionId = answer.field.id,
                                    value = answer.value ?: "",
                                )
                            } ?: emptyList(),
                    )
                },
        )
    }
}
