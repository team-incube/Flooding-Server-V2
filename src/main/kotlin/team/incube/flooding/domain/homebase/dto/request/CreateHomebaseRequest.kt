package team.incube.flooding.domain.homebase.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import team.incube.flooding.domain.homebase.dto.MemberDto
import java.time.LocalDate

data class CreateHomebaseRequest(
    val reservationDate: LocalDate,
    val startPeriod: Int,
    val endPeriod: Int,
    @field:NotBlank(message = "예약 사유를 입력해주세요.")
    @field:Size(max = 300, message = "예약 사유는 300자 이내로 입력해주세요.")
    val reason: String,
    @field:NotEmpty(message = "예약 인원은 최소 1명 이상이어야 합니다.")
    val members: List<MemberDto>,
)
