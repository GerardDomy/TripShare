package com.example.tripshare.Account

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.tripshare.R

class ViewImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        val imageView = findViewById<ImageView>(R.id.imageViewFull)
        val imageUriString = intent.getStringExtra("imageUri")
        imageUriString?.let {
            val imageUri = Uri.parse(it)
            imageView.setImageURI(imageUri)
        }
    }
}
