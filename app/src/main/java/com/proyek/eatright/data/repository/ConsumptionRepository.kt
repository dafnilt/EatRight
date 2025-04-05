package com.proyek.eatright.data.repository

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.proyek.eatright.data.model.DailyConsumption
import kotlinx.coroutines.tasks.await

class ConsumptionRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val consumptionsCollection by lazy { firestore.collection("consumptions") }

    suspend fun addConsumption(consumption: DailyConsumption): Result<DailyConsumption> {
        return try {
            val consumptionWithId = if (consumption.id.isEmpty()) {
                consumption.copy(id = consumptionsCollection.document().id)
            } else {
                consumption
            }

            consumptionsCollection.document(consumptionWithId.id).set(consumptionWithId).await()
            Result.success(consumptionWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserConsumptions(userId: String, date: Long? = null): Result<List<DailyConsumption>> {
        return try {
            var query: Query = consumptionsCollection.whereEqualTo("userId", userId)

            // Filter by date if provided
            if (date != null) {
                // Get start and end of the day in milliseconds
                val startOfDay = getStartOfDayTimestamp(date)
                val endOfDay = getEndOfDayTimestamp(date)

                query = query.whereGreaterThanOrEqualTo("timestamp", startOfDay)
                    .whereLessThanOrEqualTo("timestamp", endOfDay)
            }

            val querySnapshot = query.get().await()
            val consumptions = querySnapshot.documents.mapNotNull {
                it.toObject(DailyConsumption::class.java)
            }

            Result.success(consumptions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteConsumption(consumptionId: String): Result<Unit> {
        return try {
            consumptionsCollection.document(consumptionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper functions for date handling
    private fun getStartOfDayTimestamp(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDayTimestamp(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}