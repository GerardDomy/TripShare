package com.example.tripshare.HomeScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tripshare.R
import com.example.tripshare.Register_Login.LauncherActivity
import com.example.tripshare.Register_Login.RegisterActivity
import de.hdodenhof.circleimageview.CircleImageView

class HomescreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homescreen)

        // Ocultar la barra de acción
        supportActionBar?.hide()

        // Text view
        val backgroundText: TextView = findViewById(R.id.textView)
        val textAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        backgroundText.startAnimation(textAnimation)

        // Image view
        val backgroundImage: CircleImageView = findViewById(R.id.imageView)
        val imageAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        backgroundImage.startAnimation(imageAnimation)

        // Retardo de la pantalla de bienvenida (Splashscreen)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LauncherActivity::class.java)
            startActivity(intent)
            finish()
            // Tiempo en milisegundos
        }, 2100)
    }
}