package team.incube.flooding.domain.neis.presentation.data.request

data class GetNeisMealsRequest(
    val officeCode: String,
    val schoolCode: String,
    val date: String,
)
