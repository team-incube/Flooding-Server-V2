package team.incube.flooding.domain.dormitory.cleaningzone.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import team.incube.flooding.domain.user.entity.UserJpaEntity

@Entity
@Table(name = "tb_cleaning_zone")
class CleaningZoneJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:Column(name = "name", nullable = false)
    var name: String,
    @field:Column(name = "description", nullable = false)
    var description: String,
    @field:OneToMany(mappedBy = "cleaningZone", fetch = FetchType.LAZY)
    val members: List<UserJpaEntity> = emptyList(),
)
