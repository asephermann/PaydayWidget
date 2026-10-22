package io.github.asephermann.paydaywidget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSharedPreferences("payday_pref", MODE_PRIVATE)
            .edit {
                putInt("payday", 25)
            }
    }
}