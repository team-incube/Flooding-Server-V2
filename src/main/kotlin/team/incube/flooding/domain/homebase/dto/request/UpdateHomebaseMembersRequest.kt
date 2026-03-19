package team.incube.flooding.domain.homebase.dto.request

import team.incube.flooding.domain.homebase.dto.MemberDto

data class UpdateHomebaseMembersRequest(
    val members: List<MemberDto>
)