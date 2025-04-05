package com.proyek.eatright

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Pastikan Firebase diinisialisasi saat aplikasi dibuka
        FirebaseApp.initializeApp(this)

        // Store context dalam companion object
        appContext = applicationContext
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}