package com.proyek.eatright.data.repository

import com.proyek.eatright.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun register(user: User): Result<FirebaseUser> {
        return try {
            // Buat akun dengan email dan password
            val authResult = auth.createUserWithEmailAndPassword(user.email, user.password).await()

            // Tambahkan data user ke Firestore (tanpa password)
            val userWithoutPassword = user.copy(
                id = authResult.user?.uid ?: UUID.randomUUID().toString(),
                password = "" // Jangan simpan password ke database
            )

            usersCollection.document(userWithoutPassword.id).set(userWithoutPassword).await()

            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(userId: String): Result<User> {
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            val user = documentSnapshot.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}