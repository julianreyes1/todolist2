package com.example.todolist2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth


    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: MaterialButton


    private lateinit var progressBarLogin: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()


        emailEditText = findViewById(R.id.et_email)
        passwordEditText = findViewById(R.id.et_password)
        loginButton = findViewById(R.id.btn_login)


        progressBarLogin = findViewById(R.id.progress_bar_login)


        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa correo y contraseña.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            showLoading(true)


            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->



                    if (task.isSuccessful) {

                        Log.d("Auth", "signInWithEmail:success")

                        updateUI(auth.currentUser)
                    } else {

                        Log.w("Auth", "signInWithEmail:failure, trying to register", task.exception)

                        showLoading(false)
                        registerUser(email, password)
                    }
                }
        }
    }


    private fun registerUser(email: String, password: String) {


        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->


                showLoading(false)

                if (task.isSuccessful) {

                    Log.d("Auth", "createUserWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(this, "Registro exitoso. ¡Bienvenido!", Toast.LENGTH_LONG).show()

                    updateUI(user)
                } else {

                    Log.w("Auth", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Fallo en autenticación o registro: ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                    updateUI(null)
                }
            }
    }


    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBarLogin.visibility = View.VISIBLE

            loginButton.isEnabled = false
        } else {
            progressBarLogin.visibility = View.GONE
            loginButton.isEnabled = true
        }
    }


    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        updateUI(currentUser)
    }


    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {

            Log.d("Auth", "User logged in: ${user.uid}")
            val intent = Intent(this, Pantalla2::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            Log.d("Auth", "User not logged in.")

        }
    }
}