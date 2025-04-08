package com.proyek.eatright.utils

import com.proyek.eatright.data.model.Serving
import com.proyek.eatright.data.model.DailyConsumption
import com.proyek.eatright.data.model.FoodDetail
import kotlin.math.roundToInt

data class DiabetesNutritionWarning(
    val type: WarningType,
    val message: String
)

enum class WarningType {
    HIGH_CARBS,
    LOW_CARBS,
    HIGH_SUGAR,
    HIGH_FAT,
    HIGH_PROTEIN
}

class DiabetesNutritionValidator {
    companion object {
        // Asumsi kebutuhan kalori harian rata-rata 2000 kkal
        private const val DAILY_CALORIES = 2000.0
        private const val SUGAR_THRESHOLD = 50.0 // 50 gram

        fun validateNutrition(serving: Serving, quantity: Int = 1): List<DiabetesNutritionWarning> {
            val warnings = mutableListOf<DiabetesNutritionWarning>()

            // Konversi nutrisi berdasarkan quantity
            val calories = serving.calories * quantity
            val carbs = serving.carbohydrate * quantity
            val sugar = serving.sugar * quantity
            val fat = serving.fat * quantity
            val protein = serving.protein * quantity

            // Validasi Karbohidrat
            val carbPercentage = (carbs * 4 / calories * 100).roundToInt()
            if (carbPercentage > 65) {
                warnings.add(
                    DiabetesNutritionWarning(
                        WarningType.HIGH_CARBS,
                        "Kandungan karbohidrat terlalu tinggi (${carbPercentage}%). Disarankan 45-65% dari total kalori."
                    )
                )
            }
            if (carbs < 130.0 / DAILY_CALORIES * calories) {
                warnings.add(
                    DiabetesNutritionWarning(
                        WarningType.LOW_CARBS,
                        "Kandungan karbohidrat rendah. Minimal 130g per hari direkomendasikan."
                    )
                )
            }

            // Validasi Gula
            if (sugar > SUGAR_THRESHOLD) {
                warnings.add(
                    DiabetesNutritionWarning(
                        WarningType.HIGH_SUGAR,
                        "Kandungan gula tinggi (${String.format("%.1f", sugar)}g). Melebihi batas aman 50g per hari."
                    )
                )
            }

            // Validasi Lemak
            val fatPercentage = (fat * 9 / calories * 100).roundToInt()
            if (fatPercentage > 30) {
                warnings.add(
                    DiabetesNutritionWarning(
                        WarningType.HIGH_FAT,
                        "Kandungan lemak terlalu tinggi (${fatPercentage}%). Disarankan 20-30% dari total kalori."
                    )
                )
            }

            // Validasi Protein (untuk pasien dengan nefropati diabetik)
            val proteinPercentage = (protein * 4 / calories * 100).roundToInt()
            if (proteinPercentage > 10) {
                warnings.add(
                    DiabetesNutritionWarning(
                        WarningType.HIGH_PROTEIN,
                        "Kandungan protein tinggi (${proteinPercentage}%). Untuk pasien nefropati diabetik, disarankan tidak lebih dari 10% total kalori."
                    )
                )
            }

            return warnings
        }

        // Fungsi untuk mendapatkan total gula dari daftar konsumsi harian
        fun calculateTotalSugar(
            userConsumptions: List<DailyConsumption>,
            foodDetailsCache: Map<String, FoodDetail>
        ): Double {
            var totalSugar = 0.0
            userConsumptions.forEach { consumption ->
                val foodDetail = foodDetailsCache[consumption.foodId]
                if (foodDetail != null &&
                    consumption.servingIndex >= 0 &&
                    consumption.servingIndex < foodDetail.servings.size) {

                    val serving = foodDetail.servings[consumption.servingIndex]
                    totalSugar += serving.sugar * consumption.quantity
                }
            }
            return totalSugar
        }
    }
}