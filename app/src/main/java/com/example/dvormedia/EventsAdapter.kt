package com.example.dvormedia

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EventsAdapter(private val events: List<Event>) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.eventTitleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.eventDescriptionTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.eventImageView)
        private val videoView: VideoView = itemView.findViewById(R.id.eventVideoView)

        private var isExpanded = false

        fun bind(event: Event) {
            titleTextView.text = event.title
            descriptionTextView.text = event.description

            // Убедимся, что элементы изначально свёрнуты
            descriptionTextView.visibility = View.GONE
            imageView.visibility = View.GONE
            videoView.visibility = View.GONE

            if (event.imageURL.isNotEmpty()) {
                val imageUri = Uri.parse(event.imageURL)
                imageView.visibility = View.GONE // Изначально свернуты
                videoView.visibility = View.GONE
                Glide.with(itemView.context).load(imageUri).into(imageView)
            } else if (event.videoURL.isNotEmpty()) {
                val videoUri = Uri.parse(event.videoURL)
                videoView.visibility = View.GONE // Изначально свернуты
                imageView.visibility = View.GONE
                videoView.setVideoURI(videoUri)
                videoView.start()
            } else {
                imageView.visibility = View.GONE
                videoView.visibility = View.GONE
            }

            itemView.findViewById<View>(R.id.cardView).setOnClickListener {
                if (isExpanded) {
                    collapse()
                } else {
                    expand()
                }
                isExpanded = !isExpanded
            }
        }

        private fun expand() {
            descriptionTextView.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
            descriptionTextView.alpha = 0f
            imageView.alpha = 0f

            descriptionTextView.animate().alpha(1.0f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(500).start()
            imageView.animate().alpha(1.0f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(500).start()
        }

        private fun collapse() {
            val animDescription = descriptionTextView.animate().alpha(0.0f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(500)
            val animImage = imageView.animate().alpha(0.0f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(500)

            animDescription.withEndAction {
                descriptionTextView.visibility = View.GONE
            }
            animImage.withEndAction {
                imageView.visibility = View.GONE
            }

            animDescription.start()
            animImage.start()
        }
    }
}