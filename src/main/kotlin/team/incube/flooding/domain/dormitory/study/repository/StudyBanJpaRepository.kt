package team.incube.flooding.domain.dormitory.study.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.dormitory.study.entity.StudyBanJpaEntity
import java.time.LocalDateTime

interface StudyBanJpaRepository : JpaRepository<StudyBanJpaEntity, Long> {
    fun existsByUserIdAndBannedUntilAfter(
        userId: Long,
        now: LocalDateTime
    ): Boolean
}
