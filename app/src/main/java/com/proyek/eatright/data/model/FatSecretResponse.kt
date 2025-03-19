package com.proyek.eatright.data.model


data class Food(
    val id: String,
    val name: String,
    val description: String,
    val type: String,
    val url: String
)

data class FoodSearchResponse(
    val foods: List<Food>
)
