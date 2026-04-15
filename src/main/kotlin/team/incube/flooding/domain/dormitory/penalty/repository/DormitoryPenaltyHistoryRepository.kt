package team.incube.flooding.domain.dormitory.penalty.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.flooding.domain.dormitory.penalty.entity.DormitoryPenaltyHistoryJpaEntity

interface DormitoryPenaltyHistoryRepository : JpaRepository<DormitoryPenaltyHistoryJpaEntity, Long>
