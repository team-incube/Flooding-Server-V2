package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyAttendanceListResponse
import team.incube.flooding.domain.dormitory.study.repository.StudyAttendanceHistoryJpaRepository
import team.incube.flooding.domain.dormitory.study.service.GetPublicStudyAttendanceListService
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class GetPublicStudyAttendanceListServiceImpl(
    private val studyAttendanceHistoryJpaRepository: StudyAttendanceHistoryJpaRepository,
) : GetPublicStudyAttendanceListService {
    override fun execute(): List<GetStudyAttendanceListResponse> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(6)
        val histories =
            studyAttendanceHistoryJpaRepository.findAllByAttendedDateBetweenOrderByAttendedDateAsc(startDate, endDate)
        val namesByDate = histories.groupBy({ it.attendedDate }, { it.user.name })
        return generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
            .map { date -> GetStudyAttendanceListResponse(date = date, students = namesByDate[date] ?: emptyList()) }
            .toList()
    }
}
