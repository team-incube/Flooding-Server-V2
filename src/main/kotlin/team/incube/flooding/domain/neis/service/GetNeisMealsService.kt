package team.incube.flooding.domain.neis.service

import team.incube.flooding.domain.neis.presentation.data.request.GetNeisMealsRequest
import team.incube.flooding.domain.neis.presentation.data.response.GetNeisMealsResponse

interface GetNeisMealsService {
    fun execute(request: GetNeisMealsRequest): GetNeisMealsResponse
}