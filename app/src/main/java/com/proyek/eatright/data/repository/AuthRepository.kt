package com.proyek.eatright.data.repository

import com.proyek.eatright.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class AuthRepository {
    // Use lazy initialization to ensure Firebase is available when needed
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val usersCollection by lazy { firestore.collection("users") }

    suspend fun checkEmailExists(email: String): Result<Boolean> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("email", email)
                .get()
                .await()

            Result.success(!querySnapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(user: User): Result<FirebaseUser> {
        return try {
            // Check if email already exists in Firestore
            checkEmailExists(user.email).fold(
                onSuccess = { emailExists ->
                    if (emailExists) {
                        return Result.failure(Exception("EMAIL_ALREADY_EXISTS"))
                    }
                },
                onFailure = { exception ->
                    return Result.failure(exception)
                }
            )

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

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            // Pastikan user sudah login
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User tidak terautentikasi"))
            }

            // Simpan email lama untuk perbandingan
            val oldEmail = currentUser.email

            // Debug logging
            android.util.Log.d("AuthRepository", "Current Firebase Auth email: $oldEmail")
            android.util.Log.d("AuthRepository", "New email to update: ${user.email}")
            android.util.Log.d("AuthRepository", "Email needs update: ${user.email != oldEmail}")

            var shouldUpdateFirestore = true
            var firestoreUser = user.copy(password = "")

            // Jika email berubah, kirim email verifikasi tapi jangan update Firestore dulu
            if (user.email != oldEmail) {
                android.util.Log.d("AuthRepository", "Email changed, sending verification email...")

                // Cek apakah email baru sudah digunakan
                checkEmailExists(user.email).fold(
                    onSuccess = { emailExists ->
                        if (emailExists) {
                            android.util.Log.e("AuthRepository", "Email already exists in Firestore")
                            return Result.failure(Exception("EMAIL_ALREADY_EXISTS"))
                        } else {
                            android.util.Log.d("AuthRepository", "Email available in Firestore")
                        }
                    },
                    onFailure = { exception ->
                        android.util.Log.e("AuthRepository", "Error checking email existence: ${exception.message}")
                        return Result.failure(exception)
                    }
                )

                try {
                    // Kirim email verifikasi untuk email baru
                    currentUser.verifyBeforeUpdateEmail(user.email).await()
                    android.util.Log.d("AuthRepository", "Verification email sent successfully")

                    // Jangan update email di Firestore sampai diverifikasi
                    // Gunakan email lama di Firestore
                    firestoreUser = user.copy(
                        email = oldEmail ?: user.email,
                        password = ""
                    )

                    android.util.Log.d("AuthRepository", "Email change pending verification. Firestore will keep old email: $oldEmail")

                } catch (e: Exception) {
                    android.util.Log.e("AuthRepository", "Failed to send verification email: ${e.message}")
                    throw e
                }
            }

            // Update data user di Firestore
            android.util.Log.d("AuthRepository", "Updating user data in Firestore...")
            android.util.Log.d("AuthRepository", "User ID: ${user.id}")
            android.util.Log.d("AuthRepository", "User data: $firestoreUser")

            usersCollection.document(user.id).set(firestoreUser).await()
            android.util.Log.d("AuthRepository", "Firestore update completed successfully")

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error in updateUserProfile: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Fungsi baru untuk mengecek dan mengupdate email setelah verifikasi
    suspend fun checkAndUpdateEmailAfterVerification(userId: String, newEmail: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User tidak terautentikasi"))
            }

            // Reload user untuk mendapatkan status verifikasi terbaru
            currentUser.reload().await()

            // Cek apakah email sudah diverifikasi dan berubah
            if (currentUser.email == newEmail) {
                android.util.Log.d("AuthRepository", "Email verified and updated in Firebase Auth: $newEmail")

                // Update email di Firestore juga
                val documentSnapshot = usersCollection.document(userId).get().await()
                val currentUserData = documentSnapshot.toObject(User::class.java)

                if (currentUserData != null) {
                    val updatedUserData = currentUserData.copy(email = newEmail)
                    usersCollection.document(userId).set(updatedUserData).await()
                    android.util.Log.d("AuthRepository", "Email updated in Firestore: $newEmail")
                }

                Result.success(Unit)
            } else {
                android.util.Log.d("AuthRepository", "Email not yet verified or changed")
                Result.failure(Exception("Email belum diverifikasi"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error checking email verification: ${e.message}")
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}