package team.incube.flooding.domain.dormitory.massage.presentation.data.response

import team.incube.flooding.domain.dormitory.massage.entity.MassageApplicationStatus

data class GetMassageListResponse(
    val isApplicationOpen: Boolean,
    val myApplicationStatus: MassageApplicationStatus?,
    val applicants: List<GetMassageResponse>,
)
