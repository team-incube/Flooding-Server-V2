package team.incube.flooding.domain.homebase.service

import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import team.incube.flooding.domain.homebase.dto.MemberDto
import team.incube.flooding.domain.homebase.entity.HomebaseMemberJpaEntity
import team.incube.flooding.domain.homebase.entity.HomebaseReservationJpaEntity
import team.incube.flooding.domain.homebase.repository.HomebaseMemberRepository
import team.themoment.sdk.exception.ExpectedException

@Service
class HomebaseMemberService(private val memberRepository: HomebaseMemberRepository) {
    @Transactional
    fun saveAllMembers(reservation: HomebaseReservationJpaEntity, memberDtos: List<MemberDto>) {
        val members = memberDtos.map {
            HomebaseMemberJpaEntity(
                studentNumber = it.studentNumber,
                name = it.name,
                reservation = reservation
            )
        }
        memberRepository.saveAll(members)
    }

    @Transactional
    fun deleteByReservationId(reservationId: Long) {
        return memberRepository.deleteByReservationId(reservationId)
    }

    @Transactional(readOnly = true)
    fun validateStudentDuplicate(startPeriod: Int, endPeriod: Int, members: List<MemberDto>, reservationId: Long? = null) {
        val studentNumbers = members.map { it.studentNumber }
        val existingStudents = memberRepository.findExistingStudentNumbersInPeriod(
            studentNumbers,
            startPeriod,
            endPeriod,
            reservationId
        )

        if (existingStudents.isNotEmpty()) {
            throw ExpectedException("이미 다른 홈베이스를 신청한 학생이 포함되어 있습니다. " , HttpStatus.BAD_REQUEST)
        }
    }
}