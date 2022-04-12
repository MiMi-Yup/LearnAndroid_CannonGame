package com.example.cannongame.sub_class

import android.graphics.Canvas
import android.graphics.Rect

class CannonBall(
    view: CannonView,
    color: Int,
    soundId: Int,
    x: Int,
    y: Int,
    radius: Int,
    velocityX: Float,
    velocityY: Float
) : GameElement(view, color, soundId, x, y, 2 * radius, 2 * radius, velocityY) {
    private var velocityX: Float = velocityX
    private var onScreen: Boolean = true

    fun getRadius(): Int = (shape.right - shape.left) / 2

    fun collidesWith(element: GameElement): Boolean =
        (Rect.intersects(shape, element.getShape()) && velocityX > 0)

    fun isOnScreen(): Boolean = onScreen

    fun reverseVelocityX(): Unit {
        velocityX *= -1
    }

    override fun update(interval: Double) {
        super.update(interval)

        shape.offset((velocityX * interval).toInt(), 0)

        if (shape.top < 0 || shape.left < 0 || shape.bottom > view.getScreenHeight() || shape.right > view.getScreenWidth()) {
            onScreen = false
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(
            (shape.left + getRadius()).toFloat(),
            (shape.top + getRadius()).toFloat(),
            getRadius().toFloat(),
            paint
        )
    }
}