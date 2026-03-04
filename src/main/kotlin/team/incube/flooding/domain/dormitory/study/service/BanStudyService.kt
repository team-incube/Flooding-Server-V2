package team.incube.flooding.domain.dormitory.study.service

interface BanStudyService {
    fun execute(targetUserId: Long)
}