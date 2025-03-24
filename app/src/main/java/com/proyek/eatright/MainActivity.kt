package com.proyek.eatright

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.proyek.eatright.ui.screen.FoodSearchApp
import com.proyek.eatright.ui.theme.EatRightTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EatRightTheme {
                FoodSearchApp()
            }
        }
    }
}

