package com.example.dvormedia

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class FullscreenImageDialog(context: Context, private val imageUrl: String) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_fullscreen_image)

        val photoView: PhotoView = findViewById(R.id.photo_view)
        Glide.with(context)
            .load(imageUrl)
            .into(photoView)
    }
}