package com.example.dvormedia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EventsAdapter(private val events: List<Event>) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
    private val expandedPositions = mutableSetOf<Int>() // Сохраняем состояние раскрытых элементов

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Убрано добавление декоратора отступов здесь, так как это должно быть реализовано в активности
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        holder.title.text = event.title

        // Определяем, раскрыт ли элемент
        val isExpanded = expandedPositions.contains(position)

        // Устанавливаем описание в зависимости от того, раскрыт элемент или нет
        val description = if (isExpanded) {
            event.description
        } else {
            if (event.description.length > 15) "${event.description.substring(0, 15)}..." else event.description
        }
        holder.description.text = description
        holder.description.maxLines = if (isExpanded) Integer.MAX_VALUE else 1

        holder.photoView.visibility = if (isExpanded && event.imageURL.isNotEmpty()) View.VISIBLE else View.GONE

        // Обработчик нажатия на описание и заголовок
        val clickListener = View.OnClickListener {
            if (expandedPositions.contains(holder.adapterPosition)) {
                expandedPositions.remove(holder.adapterPosition)
            } else {
                expandedPositions.add(holder.adapterPosition)
            }
            notifyItemChanged(holder.adapterPosition) // Обновляем элемент с новым состоянием
        }

        holder.description.setOnClickListener(clickListener)
        holder.title.setOnClickListener(clickListener) // Используем тот же обработчик, что и для описания

        // Загрузка изображения, если описание раскрыто
        if (isExpanded && event.imageURL.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(event.imageURL)
                .into(holder.photoView)
        }
    }

    override fun getItemCount() = events.size


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.eventTitle)
        val description: TextView = itemView.findViewById(R.id.eventDescription)
        val photoView: ImageView = itemView.findViewById(R.id.photoView)
    }
}