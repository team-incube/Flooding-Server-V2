package team.incube.flooding.domain.dormitory.study.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import team.incube.flooding.domain.dormitory.study.entity.StudyBanJpaEntity
import team.incube.flooding.domain.user.entity.UserJpaEntity
import java.time.LocalDateTime

interface StudyBanJpaRepository : JpaRepository<StudyBanJpaEntity, Long> {
    fun existsByUserAndBannedUntilAfter(
        user: UserJpaEntity,
        now: LocalDateTime
    ): Boolean
}