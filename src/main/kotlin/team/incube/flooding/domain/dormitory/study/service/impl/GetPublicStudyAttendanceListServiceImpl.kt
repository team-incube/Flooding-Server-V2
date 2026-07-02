package team.incube.flooding.domain.dormitory.study.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.incube.flooding.domain.dormitory.study.presentation.data.response.GetStudyAttendanceListResponse
import team.incube.flooding.domain.dormitory.study.repository.StudyAttendanceHistoryRepository
import team.incube.flooding.domain.dormitory.study.service.GetPublicStudyAttendanceListService
import java.time.Clock
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class GetPublicStudyAttendanceListServiceImpl(
    private val studyAttendanceHistoryRepository: StudyAttendanceHistoryRepository,
    private val clock: Clock,
) : GetPublicStudyAttendanceListService {
    override fun execute(): List<GetStudyAttendanceListResponse> {
        val endDate = LocalDate.now(clock)
        val startDate = endDate.minusDays(6)
        val histories =
            studyAttendanceHistoryRepository.findAllByAttendedDateBetweenOrderByAttendedDateAsc(startDate, endDate)
        val namesByDate = histories.groupBy({ it.attendedDate }, { it.user.name })
        return (0..6).map { i ->
            val date = startDate.plusDays(i.toLong())
            GetStudyAttendanceListResponse(date = date, students = namesByDate[date] ?: emptyList())
        }
    }
}
