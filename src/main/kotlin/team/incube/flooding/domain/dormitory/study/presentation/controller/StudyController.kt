package team.incube.flooding.domain.dormitory.study.presentation.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.dormitory.study.service.BanStudyService
import team.incube.flooding.domain.dormitory.study.service.CancelStudyService
import team.incube.flooding.domain.dormitory.study.service.StudyApplicationService
import team.themoment.sdk.response.CommonApiResponse

@RestController
@RequestMapping("/study")
class StudyController(
    private val studyApplicationService: StudyApplicationService,
    private val cancelStudyService: CancelStudyService,
    private val banStudyService: BanStudyService
) {

    @PostMapping
    fun apply(): CommonApiResponse<Nothing> {
        studyApplicationService.execute()
        return CommonApiResponse.success("OK")
    }
    @DeleteMapping
    fun cancel(): CommonApiResponse<Nothing> {
        cancelStudyService.execute()
        return CommonApiResponse.success("OK")
    }
    @PostMapping("/ban/{userId}")
    fun ban(@PathVariable userId: Long): CommonApiResponse<Nothing> {
        banStudyService.execute(userId)
        return CommonApiResponse.success("OK")
    }
}
