package team.incube.flooding.domain.neis.client.dto

data class GetMealsRequest(
    val officeCode: String,
    val schoolCode: String,
    val date: String,
)
