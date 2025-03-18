package com.example.tripshare.Account

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatDelegate
import com.example.tripshare.R
import com.example.tripshare.Register_Login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnLogout: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var btnBack: ImageButton
    private lateinit var btnDarkMode: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        btnLogout = findViewById(R.id.btnLogout)
        btnBack = findViewById(R.id.btnBack)
        btnDarkMode = findViewById(R.id.switchDarkMode)

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnLogout.setOnClickListener {
            logout()
        }
        // Al hacer clic en el botón, alternamos entre modo oscuro y claro
        btnDarkMode.setOnClickListener {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
                enableDarkMode()
            } else {
                disableDarkMode()
            }
        }
    }


    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Activar el modo oscuro
    private fun enableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        delegate.applyDayNight()
    }

    // Desactivar el modo oscuro
    private fun disableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        delegate.applyDayNight()
    }
}
