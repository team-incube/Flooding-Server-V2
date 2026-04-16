package team.incube.flooding.domain.dormitory.cleaningzone.presentation.data.request

import jakarta.validation.constraints.NotBlank

data class CreateCleaningZoneRequest(
    @field:NotBlank(message = "구역 이름은 필수입니다.")
    val name: String,
    @field:NotBlank(message = "구역 설명은 필수입니다.")
    val description: String,
)
