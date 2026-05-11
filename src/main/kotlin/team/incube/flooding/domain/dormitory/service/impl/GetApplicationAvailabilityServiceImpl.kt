package team.incube.flooding.domain.dormitory.service.impl

import org.springframework.stereotype.Service
import team.incube.flooding.domain.dormitory.massage.config.MassageProperties
import team.incube.flooding.domain.dormitory.presentation.data.response.GetApplicationAvailabilityResponse
import team.incube.flooding.domain.dormitory.service.GetApplicationAvailabilityService
import team.incube.flooding.domain.dormitory.study.config.StudyProperties
import java.time.Clock
import java.time.LocalTime

@Service
class GetApplicationAvailabilityServiceImpl(
    private val studyProperties: StudyProperties,
    private val massageProperties: MassageProperties,
    private val clock: Clock,
) : GetApplicationAvailabilityService {
    override fun execute(): GetApplicationAvailabilityResponse {
        val now = LocalTime.now(clock)
        return GetApplicationAvailabilityResponse(
            study = isStudyAvailable(now),
            homebase = true,
            massage = isMassageAvailable(now),
        )
    }

    private fun isStudyAvailable(now: LocalTime): Boolean {
        val crossesMidnight = studyProperties.openTime.isAfter(studyProperties.closeTime)
        val outOfRange =
            if (crossesMidnight) {
                now.isBefore(studyProperties.openTime) && now.isAfter(studyProperties.closeTime)
            } else {
                now.isBefore(studyProperties.openTime) || now.isAfter(studyProperties.closeTime)
            }
        return !outOfRange
    }

    private fun isMassageAvailable(now: LocalTime): Boolean = !now.isBefore(massageProperties.openTime)
}
