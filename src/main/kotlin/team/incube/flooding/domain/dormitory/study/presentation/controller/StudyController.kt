package team.incube.flooding.domain.dormitory.study.presentation.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.incube.flooding.domain.dormitory.study.service.BanStudyService
import team.incube.flooding.domain.dormitory.study.service.CancelStudyService
import team.incube.flooding.domain.dormitory.study.service.StudyApplicationService

@RestController
@RequestMapping("/study")
class StudyController(
    private val studyApplicationService: StudyApplicationService,
    private val cancelStudyService: CancelStudyService,
    private val banStudyService: BanStudyService
) {

    @PostMapping
    fun apply(): ResponseEntity<Unit> {
        studyApplicationService.execute()
        return ResponseEntity.ok().build()
    }

    @DeleteMapping
    fun cancel(): ResponseEntity<Unit> {
        cancelStudyService.execute()
        return ResponseEntity.ok().build()
    }

    @PostMapping("/ban/{userId}")
    fun ban(@PathVariable userId: Long):ResponseEntity<Unit> {
        banStudyService.execute(userId)
        return ResponseEntity.ok().build()
    }
}

