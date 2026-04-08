package team.incube.flooding.domain.club.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "tb_club_form_field",
    indexes = [Index(name = "idx_club_form_field_order", columnList = "form_id, field_order")],
)
class ClubFormFieldJpaEntity(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "form_id", nullable = false)
    val form: ClubFormJpaEntity,
    @field:Column(name = "label", nullable = false)
    var label: String,
    @field:Column(name = "description")
    var description: String?,
    @field:Column(name = "field_type", nullable = false, length = 50)
    @field:Enumerated(EnumType.STRING)
    var fieldType: ClubFormFieldType,
    @field:Column(name = "field_order", nullable = false)
    var fieldOrder: Int,
    @field:Column(name = "is_required", nullable = false)
    var isRequired: Boolean,
)
