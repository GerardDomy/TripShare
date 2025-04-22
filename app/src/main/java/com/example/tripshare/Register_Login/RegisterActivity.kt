package com.example.tripshare.Register_Login

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.tripshare.MainActivity
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RegisterActivity : AppCompatActivity() {

    // Declaración de las variables para los elementos de la interfaz
    private lateinit var registerEmail: EditText
    private lateinit var registerPassword: EditText
    private lateinit var registerRepeatPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var registerGoLoginButton: Button

    // Inicialización de FirebaseAuth y Firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Permite un diseño de pantalla completa
        setContentView(R.layout.activity_register) // Configura el layout de la actividad

        // Inicializa FirebaseAuth y Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializa los elementos de la interfaz
        registerEmail = findViewById(R.id.registerEmail)
        registerPassword = findViewById(R.id.registerPassword)
        registerRepeatPassword = findViewById(R.id.registerRepeatPassword)
        registerButton = findViewById(R.id.registerButton)
        registerGoLoginButton = findViewById(R.id.registerGoLoginButton)

        // Configura el botón de registro
        registerButton.setOnClickListener {
            val email = registerEmail.text.toString()
            val password = registerPassword.text.toString()
            val repeatPassword = registerRepeatPassword.text.toString()

            // Verifica si la contraseña y la repetición de la contraseña coinciden y si los campos no están vacíos
            if (password == repeatPassword && checkEmpty(email, password, repeatPassword)) {
                // Si la validación es exitosa, llama al método para registrar el usuario
                register(email, password)
            }
            else
            {
                // Si las contraseñas no coinciden, muestra un mensaje de error
                Toast.makeText(applicationContext, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura el botón para navegar a la actividad de login
        registerGoLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java)) // Ir a la actividad de login
            finish()
        }
    }

    // Método para registrar al usuario en Firebase
    private fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                        val userData = hashMapOf(
                            "uid" to it.uid,
                            "email" to it.email,
                            "devices" to listOf(deviceId)
                        )
                        db.collection("users").document(it.uid).set(userData)
                            .addOnSuccessListener {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(applicationContext, "Error saving user data", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(applicationContext, "Register failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }


    // Método para verificar si los campos de email, contraseña y repetición de contraseña no están vacíos
    private fun checkEmpty(email: String, password: String, repeatPassword: String): Boolean {
        // Verifica que ninguno de los campos esté vacío
        return email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()
    }
}