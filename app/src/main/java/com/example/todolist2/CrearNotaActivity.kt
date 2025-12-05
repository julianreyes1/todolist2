package com.example.todolist2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.MenuItem
import com.example.todolist2.Note


class CrearNotaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var editTitulo: EditText
    private lateinit var editContenido: EditText
    private lateinit var btnGuardar: Button

    private lateinit var radioGroupPrioridad: RadioGroup
    private lateinit var radioUrgente: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_nota)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }



        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        editTitulo = findViewById(R.id.edit_text_titulo)
        editContenido = findViewById(R.id.edit_text_contenido)
        btnGuardar = findViewById(R.id.button_guardar)

        radioGroupPrioridad = findViewById(R.id.radio_group_prioridad)
        radioUrgente = findViewById(R.id.radio_urgente)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Crear Nueva Nota"

        btnGuardar.setOnClickListener {
            saveNote()
        }
    }


    private fun saveNote() {
        val title = editTitulo.text.toString().trim()
        val content = editContenido.text.toString().trim()
        val userId = auth.currentUser?.uid


        val selectedCategory = if (radioUrgente.isChecked) "URGENTE" else "Normal"

        if (userId == null) {
            Toast.makeText(this, "Error: Inicia sesión para guardar notas.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(
                this,
                "Por favor, completa el título y el contenido.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        val newNote = Note(
            userId = userId,
            title = title,
            content = content,
            category = selectedCategory
        )

        db.collection("users").document(userId).collection("notes")
            .add(newNote)
            .addOnSuccessListener {

                Toast.makeText(this, "Nota guardada con éxito.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->

                Toast.makeText(this, "Error al guardar nota: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {

            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}