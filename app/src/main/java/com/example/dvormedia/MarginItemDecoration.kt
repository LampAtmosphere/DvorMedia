package com.example.dvormedia

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val spaceSize: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val space = spaceSize
        outRect.left = space
        outRect.right = space
        outRect.bottom = space

        // Добавляем отступ сверху только к первому элементу
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = space
        }
    }
}

fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density + 0.5f).toInt()
}