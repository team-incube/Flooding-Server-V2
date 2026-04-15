package team.incube.flooding.domain.neis.presentation.data.response

data class GetNeisMealsResponse(
    val date: String,
    val meals: List<Meal>,
) {
    data class Meal(
        val mealType: String,
        val menus: List<String>,
        val calories: String?,
    )
}

