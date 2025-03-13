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

    // Declaración de variables para los elementos de la interfaz
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var loginGoRegisterButton: Button

    // Inicialización de FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Permite un diseño de pantalla completa (con el contenido extendido hasta los bordes)
        setContentView(R.layout.activity_login) // Configurar el layout de la actividad

        auth = FirebaseAuth.getInstance() // Inicializa FirebaseAuth

        // Inicializar los elementos de la interfaz
        loginEmail = findViewById(R.id.loginEmail)
        loginPassword = findViewById(R.id.loginPassword)
        loginButton = findViewById(R.id.loginButton)
        loginGoRegisterButton = findViewById(R.id.loginGoRegisterButton)

        // Configurar el botón de login
        loginButton.setOnClickListener()
        {
            // Obtener los valores de email y contraseña desde los campos de texto
            val email = loginEmail.text.toString()
            val password = loginPassword.text.toString()

            // Verificar si los campos están vacíos
            if(checkEmpty(email, password))
            {
                // Si los campos no están vacíos, llamar al método de login
                login(email, password)
            }

        }

        // Configurar el botón para ir a la pantalla de registro
        loginGoRegisterButton.setOnClickListener()
        {
            startActivity(Intent(this, RegisterActivity::class.java)) // Ir a la actividad de registro
            finish() // Finalizar la actividad actual
        }

    }

    // Método para realizar el login con el email y la contraseña proporcionados
    private fun login(email: String, password: String)
    {
        auth.signInWithEmailAndPassword(email, password) // Intentar hacer login con Firebase
            .addOnCompleteListener(this){task ->
                // Comprobar si el login fue exitoso
                if (task.isSuccessful)
                {
                    // Si es exitoso, redirigir a la MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else
                {
                    // Si el login falla, mostrar un mensaje de error
                    Toast.makeText(applicationContext, "Login failed!", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Método para verificar si los campos de email y contraseña no están vacíos
    private fun checkEmpty(email: String, password: String): Boolean
    {
        // Retorna true si ambos campos no están vacíos
        return email.isNotEmpty() && password.isNotEmpty()
    }
}