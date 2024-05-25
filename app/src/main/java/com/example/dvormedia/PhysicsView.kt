package com.example.dvormedia

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World

class PhysicsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private val thread: Thread = Thread(this)
    private var running = false
    private lateinit var world: World
    private val paint = Paint()
    private val letters = mutableListOf<Pair<Body, String>>()

    init {
        holder.addCallback(this)
        paint.color = Color.RED
        paint.style = Paint.Style.FILL
        paint.textSize = 50f // Установим размер текста
    }

    fun setWorld(world: World) {
        this.world = world
    }

    fun addLetterBody(body: Body, letter: String) {
        letters.add(Pair(body, letter))
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        thread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try {
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        while (running) {
            val canvas: Canvas = holder.lockCanvas()
            if (canvas != null) {
                synchronized(holder) {
                    updatePhysics()
                    drawCanvas(canvas)
                }
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun updatePhysics() {
        world.step(1 / 60f, 6, 2)
    }

    private fun drawCanvas(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)
        for ((body, letter) in letters) {
            val pos = body.position
            canvas.drawText(letter, pos.x * 50, pos.y * 50, paint)
        }
    }
}