package team.incube.flooding.domain.neis.service.impl

import com.fasterxml.jackson.databind.JsonNode
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
        val targets =
            listOf(
                response.path("data"),
                response.path("meals"),
                response,
            )

        val mealNodes =
            targets.firstNotNullOfOrNull { node ->
                when {
                    node.isArray -> node
                    node.path("meals").isArray -> node.path("meals")
                    node.path("data").isArray -> node.path("data")
                    else -> null
                }
            } ?: return emptyList()

        return mealNodes.map { mealNode ->
            val menuText = valueOf(mealNode, "menu", "menus", "DDISH_NM")
            GetNeisMealsResponse.Meal(
                mealType = valueOf(mealNode, "mealType", "meal_type", "MMEAL_SC_NM") ?: "UNKNOWN",
                menus = parseMenus(menuText),
                calories = valueOf(mealNode, "calories", "calorie", "CAL_INFO"),
            )
        }
    }

    private fun parseMenus(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value
            .replace("<br/>", "\n")
            .replace("<br>", "\n")
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun valueOf(
        node: JsonNode,
        vararg keys: String,
    ): String? {
        keys.forEach { key ->
            val value = node.path(key)
            if (!value.isMissingNode && !value.isNull) return value.asText()
        }
        return null
    }
}
