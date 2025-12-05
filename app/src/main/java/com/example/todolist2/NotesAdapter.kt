package com.example.todolist2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


class NotesAdapter(
    private val notes: List<Note>,
    private val onDeleteClicked: (String) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {


    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.note_title)
        val content: TextView = itemView.findViewById(R.id.note_content)
        val categoryTag: TextView = itemView.findViewById(R.id.category_tag)
        val date: TextView = itemView.findViewById(R.id.note_date)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }


    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title
        holder.content.text = note.content


        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.date.text = "Creada: ${dateFormat.format(Date(note.timestamp))}"


        holder.categoryTag.text = note.category


        holder.deleteButton.setOnClickListener {
            onDeleteClicked(note.id)
        }


        val context = holder.itemView.context
        val colorResId = when (note.category.uppercase(Locale.ROOT)) {
            "URGENTE" -> android.R.color.holo_red_dark
            else -> android.R.color.holo_green_dark
        }
        holder.categoryTag.setBackgroundColor(context.getColor(colorResId))



        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Nota clickeada: ${note.title}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun getItemCount(): Int = notes.size
}