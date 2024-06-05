package com.example.dvormedia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PeopleAdapter(
    private val peopleList: List<Person>,
    private val isAdmin: Boolean,
    private val onLongClick: (Person) -> Unit
) : RecyclerView.Adapter<PeopleAdapter.PersonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_person, parent, false)
        return PersonViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = peopleList[position]
        holder.bind(person)

        if (isAdmin) {
            holder.itemView.setOnLongClickListener {
                onLongClick(person)
                true
            }
        }
    }

    override fun getItemCount() = peopleList.size

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.person_name)

        fun bind(person: Person) {
            nameTextView.text = person.name
        }
    }
}