package team.incube.flooding.domain.neis.service.impl

import tools.jackson.databind.JsonNode
import org.springframework.stereotype.Service
import team.incube.flooding.domain.neis.client.DgMealsClient
import team.incube.flooding.domain.neis.client.dto.GetMealsRequest
import team.incube.flooding.domain.neis.presentation.data.request.GetNeisMealsRequest
import team.incube.flooding.domain.neis.presentation.data.response.GetNeisMealsResponse
import team.incube.flooding.domain.neis.service.GetNeisMealsService

@Service
class GetNeisMealsServiceImpl(
    private val dgMealsClient: DgMealsClient,
) : GetNeisMealsService {
    override fun execute(request: GetNeisMealsRequest): GetNeisMealsResponse {
        val response =
            dgMealsClient.getMeals(
                GetMealsRequest(
                    date = request.date,
                ),
            )
        return GetNeisMealsResponse(
            date = request.date,
            meals = extractMeals(response),
        )
    }

    private fun extractMeals(response: JsonNode): List<GetNeisMealsResponse.Meal> {
        val mealNodes = response.path("data")
        if (!mealNodes.isArray) return emptyList()

        return mealNodes.map { mealNode ->
            GetNeisMealsResponse.Meal(
                mealType = mealNode.path("mealType").asText("UNKNOWN"),
                menus = mealNode.path("mealMenu").map { it.asText() },
                calories = mealNode.path("mealCalories").takeIf { !it.isNull && !it.isMissingNode }?.asText(),
            )
        }
    }
}
