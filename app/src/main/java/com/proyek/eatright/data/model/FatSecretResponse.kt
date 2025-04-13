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

// Data Models for Food Details
data class FoodDetail(
    val foodId: String,
    val foodName: String,
    val foodType: String,
    val foodUrl: String,
    val servings: List<Serving>
)

data class Serving(
    val servingId: String,
    val servingDescription: String,
    val servingUrl: String,
    val metricServingAmount: Double,
    val metricServingUnit: String,
    val numberOfUnits: Double,
    val measurementDescription: String,
    val calories: Int,
    val carbohydrate: Double,
    val protein: Double,
    val fat: Double,
    val saturatedFat: Double,
    val polyunsaturatedFat: Double,
    val monounsaturatedFat: Double,
    val cholesterol: Double,
    val sodium: Double,
    val potassium: Double,
    val fiber: Double,
    val sugar: Double,
    val vitaminA: Double,
    val vitaminC: Double,
    val calcium: Double,
    val iron: Double
)



