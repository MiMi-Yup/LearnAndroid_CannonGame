package com.example.cannongame.sub_class

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

open class GameElement {
    protected var view: CannonView
    protected var paint: Paint = Paint()
    protected var shape: Rect
    protected var soundId: Int
    protected var velocityY: Float = 0f

    constructor(
        view: CannonView,
        color: Int,
        soundId: Int,
        x: Int,
        y: Int,
        width: Int,
        length: Int,
        velocityY: Float
    ) {
        this.view = view
        this.paint.setColor(color)
        this.shape = Rect(x, y, x + width, y + length)
        this.soundId = soundId
        this.velocityY = velocityY
    }

    open fun update(interval: Double): Unit {
        shape.offset(0, (velocityY!! * interval).toInt())

        if (shape.top < 0 && velocityY < 0 || shape.bottom > view.getScreenHeight() && velocityY > 0) {
            velocityY *= -1
        }
    }

    open fun draw(canvas: Canvas): Unit {
        canvas.drawRect(shape, paint)
    }

    fun playSound(): Unit {
        view.playSound(soundId)
    }

    @JvmName("getShape1")
    fun getShape(): Rect = shape
}