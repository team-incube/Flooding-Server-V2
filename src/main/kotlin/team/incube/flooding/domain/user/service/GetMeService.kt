package team.incube.flooding.domain.user.service

import team.incube.flooding.domain.user.presentation.data.response.GetMeResponse

interface GetMeService {
    fun execute(): GetMeResponse
}
