package team.incube.flooding.domain.homebase.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.homebase.entity.HomebaseMemberJpaEntity

interface HomebaseMemberRepository : JpaRepository<HomebaseMemberJpaEntity, Long> {

    fun findByHomebaseId(homebaseId: Long): List<HomebaseMemberJpaEntity>

    fun countByHomebaseId(homebaseId: Long): Int

    fun existsByStudentNumberAndHomebase_Period(
        studentNumber: String,
        period: Int
    ): Boolean
}