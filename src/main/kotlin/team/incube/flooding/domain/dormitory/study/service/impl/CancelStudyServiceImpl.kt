package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.entity.StudyApplicationStatus
import team.incube.flooding.domain.dormitory.study.repository.StudyRedisAdapter
import team.incube.flooding.domain.dormitory.study.service.CancelStudyService
import team.incube.flooding.domain.user.entity.UserJpaEntity
import team.incube.flooding.global.security.util.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
@Transactional
class CancelStudyServiceImpl(
    private val studyRedisAdapter: StudyRedisAdapter,
    private val currentUserProvider: CurrentUserProvider
) : CancelStudyService {

    override fun execute() {

        var user = currentUserProvider.getCurrentUser()

        val stutus = studyRedisAdapter.getApplicationStatus(user.id)
        if(stutus != StudyApplicationStatus.APPROVED){
            throw ExpectedException("자습 신청 내역이 없습니다.", HttpStatus.NOT_FOUND)
        }

        studyRedisAdapter.saveApplication(user.id,StudyApplicationStatus.CANCELLED)
        studyRedisAdapter.decrementCount()
    }
}