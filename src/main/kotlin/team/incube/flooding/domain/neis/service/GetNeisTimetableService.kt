package team.incube.flooding.domain.neis.service

import team.incube.flooding.domain.neis.presentation.data.request.GetNeisTimetablesRequest
import team.incube.flooding.domain.neis.presentation.data.response.GetNeisTimetablesResponse

interface GetNeisTimetablesService {
    fun execute(request: GetNeisTimetablesRequest): GetNeisTimetablesResponse
}