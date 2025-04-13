package com.proyek.eatright.data.model

import java.util.*

data class DailyConsumption(
    val id: String = "",
    val userId: String = "",
    val foodId: String = "",          // Referensi ke foodId dari API
    val servingIndex: Int = 0,        // Indeks serving yang dipilih dalam array servings
    val quantity: Int = 1,            // Jumlah porsi
    val timestamp: Long = System.currentTimeMillis(),
    val mealType: String = "Lainnya",  // Sarapan, Makan Siang, Makan Malam, Camilan, Lainnya

    // Nutritional details
    val nutritionalDetails: NutritionalDetails = NutritionalDetails()
)

data class NutritionalDetails(
    val calories: Int = 0,
    val carbohydrate: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val saturatedFat: Double = 0.0,
    val polyunsaturatedFat: Double = 0.0,
    val monounsaturatedFat: Double = 0.0,
    val cholesterol: Double = 0.0,
    val sodium: Double = 0.0,
    val potassium: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val vitaminA: Double = 0.0,
    val vitaminC: Double = 0.0,
    val calcium: Double = 0.0,
    val iron: Double = 0.0
)