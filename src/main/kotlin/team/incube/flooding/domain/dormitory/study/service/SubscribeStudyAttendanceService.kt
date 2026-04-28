package team.incube.flooding.domain.dormitory.study.service

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface SubscribeStudyAttendanceService {
    fun execute(): SseEmitter
}
