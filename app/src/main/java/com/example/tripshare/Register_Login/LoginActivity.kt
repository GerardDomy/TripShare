package com.example.tripshare.Register_Login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.tripshare.MainActivity
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var loginGoRegisterButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        loginEmail = findViewById(R.id.loginEmail)
        loginPassword = findViewById(R.id.loginPassword)
        loginButton = findViewById(R.id.loginButton)
        loginGoRegisterButton = findViewById(R.id.loginGoRegisterButton)

        loginButton.setOnClickListener()
        {
            val email = loginEmail.text.toString()
            val password = loginPassword.text.toString()

            if(checkEmpty(email, password))
            {
                login(email, password)

            }

        }

        loginGoRegisterButton.setOnClickListener()
        {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

    }

    private fun login(email: String, password: String)
    {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task ->
                if (task.isSuccessful)
                {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else
                {
                    Toast.makeText(applicationContext, "Login failed!", Toast.LENGTH_LONG).show()

                }
            }
    }

    private fun checkEmpty(email: String, password: String): Boolean
    {
        return email.isNotEmpty() && password.isNotEmpty()

    }
}