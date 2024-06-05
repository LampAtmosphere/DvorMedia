package com.example.dvormedia

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.PhotoView

data class Photo(val url: String, val description: String)

class PhotosAdapter(private val context: Context, private val photos: List<Photo>) : RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        val requestOptions = RequestOptions().transform(RoundedCorners(60))

        Glide.with(context)
            .load(photo.url)
            .apply(requestOptions)
            .into(holder.photoImageView)

        holder.photoImageView.setOnClickListener {
            val dialog = FullscreenImageDialog(context, photo.url)
            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImageView: PhotoView = itemView.findViewById(R.id.photo_image_view)
    }
}