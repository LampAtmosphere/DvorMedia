package com.example.dvormedia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class PhotoViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)

        val photoView: PhotoView = findViewById(R.id.photo_view)
        val photoUrl = intent.getStringExtra("photo_url")

        Glide.with(this)
            .load(photoUrl)
            .into(photoView)
    }
}