package com.proyek.eatright.data.model

import java.util.*

data class User(
    val id: String = UUID.randomUUID().toString(),
    val nama: String = "",
    val email: String = "",
    val username: String? = null,
    val password: String = "", // Catatan: Password tidak boleh disimpan ke Firestore
    val noTelp: String = "",
    val tanggalLahir: String = "",
    val tinggiBadan: Int = 0,
    val beratBadan: Int = 0
)