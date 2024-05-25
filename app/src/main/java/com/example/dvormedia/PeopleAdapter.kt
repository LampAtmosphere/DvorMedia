package com.example.dvormedia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PeopleAdapter(private val peopleList: List<Person>, private val onPersonLongClick: (Person) -> Unit) : RecyclerView.Adapter<PeopleAdapter.PersonViewHolder>() {

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.person_name_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_person, parent, false)
        return PersonViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = peopleList[position]
        holder.nameTextView.text = person.name

        holder.itemView.setOnLongClickListener {
            onPersonLongClick(person)
            true
        }
    }

    override fun getItemCount(): Int {
        return peopleList.size
    }
}