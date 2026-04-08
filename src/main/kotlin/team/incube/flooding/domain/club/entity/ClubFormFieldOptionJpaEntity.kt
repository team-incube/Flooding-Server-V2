package team.incube.flooding.domain.club.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "tb_club_form_field_option")
class ClubFormFieldOptionJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "field_id", nullable = false)
    val field: ClubFormFieldJpaEntity,
    @field:Column(name = "label", nullable = false)
    val label: String,
    @field:Column(name = "value", nullable = false)
    val value: String,
    @field:Column(name = "option_order", nullable = false)
    val optionOrder: Int,
)
