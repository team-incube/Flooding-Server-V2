package team.incube.flooding.domain.homebase.service

import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import team.incube.flooding.domain.homebase.dto.MemberDto
import team.incube.flooding.domain.homebase.dto.request.CreateHomebaseRequest
import team.incube.flooding.domain.homebase.dto.request.UpdateHomebaseMembersRequest
import team.incube.flooding.domain.homebase.dto.response.GetHomebaseResponse
import team.incube.flooding.domain.homebase.entity.HomebaseMemberJpaEntity
import team.incube.flooding.domain.homebase.entity.HomebaseReservationJpaEntity
import team.incube.flooding.domain.homebase.repository.HomebaseMemberRepository
import team.incube.flooding.domain.homebase.repository.HomebaseRepository
import team.incube.flooding.domain.homebase.repository.HomebaseReservationRepository

@Service
class HomebaseService(

    private val homebaseRepository: HomebaseRepository,
    private val reservationRepository: HomebaseReservationRepository,
    private val memberRepository: HomebaseMemberRepository

) {

    @Transactional
    fun createReservation(homebaseId: Long, request: CreateHomebaseRequest) {
        val homebase = homebaseRepository.findById(homebaseId)
            .orElseThrow { IllegalArgumentException("홈베이스가 존재하지 않습니다.") }

        validateCapacity(homebase.capacity, request.members.size)
        validateReservationOverlap(homebase.id, request.startPeriod, request.endPeriod)
        validateStudentDuplicate(request.startPeriod, request.endPeriod, request.members)

        val reservation = reservationRepository.save(
            HomebaseReservationJpaEntity(
                startPeriod = request.startPeriod,
                endPeriod = request.endPeriod,
                homebase = homebase
            )
        )

        val members = request.members.map {
            HomebaseMemberJpaEntity(
                studentNumber = it.studentNumber,
                name = it.name,
                reservation = reservation
            )
        }
        memberRepository.saveAll(members)
    }

    @Transactional(readOnly = true)
    fun getReservationList(): List<GetHomebaseResponse> {
        return reservationRepository.findAllWithMembers().map { it.toResponse() }
    }

    @Transactional
    fun deleteReservation(reservationId: Long) {

        memberRepository.deleteByReservationId(reservationId)

        reservationRepository.deleteById(reservationId)
    }

    @Transactional
    fun updateReservationMembers(
        reservationId: Long,
        request: UpdateHomebaseMembersRequest
    ) {

        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { IllegalArgumentException("예약이 존재하지 않습니다.") }

        validateCapacity(reservation.homebase.capacity, request.members.size)
        validateStudentDuplicate(reservation.startPeriod, reservation.endPeriod, request.members)

        memberRepository.deleteByReservationId(reservationId)

        val members = request.members.map {
            HomebaseMemberJpaEntity(
                studentNumber = it.studentNumber,
                name = it.name,
                reservation = reservation
            )
        }
        memberRepository.saveAll(members)
    }

    private fun validateCapacity(
        capacity: Int,
        memberCount: Int
    ) {
        if (memberCount > capacity) {
            throw IllegalArgumentException("정원을 초과했습니다.")
        }
    }

    private fun validateReservationOverlap(
        homebaseId: Long,
        start: Int,
        end: Int
    ) {

        val overlapping =
            reservationRepository.findOverlappingReservation(
                homebaseId,
                start,
                end
            )

        if (overlapping.isNotEmpty()) {
            throw IllegalArgumentException("이미 예약된 시간입니다.")
        }
    }

    private fun validateStudentDuplicate(
        startPeriod: Int,
        endPeriod: Int,
        members: List<MemberDto>
    ) {
        val studentNumbers = members.map { it.studentNumber }
        val existingStudents = memberRepository.findExistingStudentNumbersInPeriod(
            studentNumbers,
            startPeriod,
            endPeriod
        )

        if (existingStudents.isNotEmpty()) {
            throw IllegalArgumentException("이미 다른 홈베이스를 신청한 학생이 포함되어 있습니다: ${existingStudents.joinToString()}")
        }
    }
}