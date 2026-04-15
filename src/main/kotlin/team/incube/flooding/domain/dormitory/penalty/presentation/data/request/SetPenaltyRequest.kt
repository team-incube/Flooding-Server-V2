package team.incube.flooding.domain.dormitory.penalty.presentation.data.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SetPenaltyRequest(
    @field:NotNull(message = "벌점 점수는 필수입니다.")
    @field:Min(value = 0, message = "벌점은 0 이상이어야 합니다.")
    val score: Int,
    @field:NotBlank(message = "변경 사유는 필수입니다.")
    val reason: String,
)
