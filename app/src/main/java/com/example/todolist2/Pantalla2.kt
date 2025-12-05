package com.example.todolist2

import android.view.ViewGroup
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


data class Note(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "Normal",
    val timestamp: Long = System.currentTimeMillis()
)

class Pantalla2 : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarLayout: com.google.android.material.appbar.AppBarLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var fabAddNote: FloatingActionButton


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    private lateinit var notesAdapter: NotesAdapter
    private val notesList = mutableListOf<Note>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla2)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
            )
            .build()

        db.firestoreSettings = settings


        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        notesRecyclerView = findViewById(R.id.notes_recycler_view)
        fabAddNote = findViewById(R.id.fab_add_note)


        appBarLayout = findViewById(R.id.app_bar_layout)


        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())


            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = systemBars.top
            view.layoutParams = params


            insets
        }

        setSupportActionBar(toolbar)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)


        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)


        updateNavHeaderEmail()


        notesAdapter = NotesAdapter(notesList, ::deleteNote)
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        notesRecyclerView.adapter = notesAdapter


        fabAddNote.setOnClickListener {

            val intent = Intent(this, CrearNotaActivity::class.java)
            startActivity(intent)
        }


        loadNotes()
    }




    private fun updateNavHeaderEmail() {
        val headerView = navigationView.getHeaderView(0)
        val emailTextView: TextView = headerView.findViewById(R.id.nav_header_user_email)
        emailTextView.text = auth.currentUser?.email ?: "Usuario Invitado"
    }


    private fun loadNotes(filterCategory: String? = null) {
        val userId = auth.currentUser?.uid ?: return
        var query: Query = db.collection("users")
            .document(userId)
            .collection("notes")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        if (filterCategory != null) {
            query = query.whereEqualTo("category", filterCategory)
        }

        query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                Toast.makeText(this, "Error al cargar notas.", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshots != null) {
                notesList.clear()
                for (doc in snapshots.documents) {
                    val note = doc.toObject(Note::class.java)?.copy(id = doc.id)
                    if (note != null) {
                        notesList.add(note)
                    }
                }
                notesAdapter.notifyDataSetChanged()
                Log.d("Firestore", "Notes loaded successfully: ${notesList.size} items.")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        loadNotes(null)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_all_notes -> {
                Log.i("NavigationDrawer", "Acción: Todas las Notas")
                loadNotes(null)
            }
            R.id.nav_filter_urgent -> {
                Log.i("NavigationDrawer", "Acción: Filtrar Urgentes")
                loadNotes("URGENTE")
            }
            R.id.nav_archive -> {
                Log.i("NavigationDrawer", "Acción: Notas Archivadas")

                Toast.makeText(this, "Funcionalidad de Archivo Pendiente", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                Log.i("NavigationDrawer", "Acción: Ajustes de la App")
                Toast.makeText(this, "Ajustes: No implementado", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                Log.i("NavigationDrawer", "Acción: Cerrar Sesión")
                logout()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun deleteNote(noteId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId).collection("notes").document(noteId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Nota eliminada con éxito.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar nota: ${e.message}", Toast.LENGTH_LONG).show()
                Log.w("Firestore", "Error deleting document", e)
            }
    }


    private fun logout() {
        auth.signOut()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}