package com.example.cannongame.base_class

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import com.example.cannongame.sub_class.CannonBall
import com.example.cannongame.sub_class.CannonView

class Cannon {
    private var baseRadius: Int
    private var barrelLength: Int
    private var barrelEnd: Point = Point()
    private var barrelAngle: Double = 0.0
    private var cannonBall: CannonBall? = null
    private var paint: Paint = Paint()
    private var view: CannonView

    constructor(
        view: CannonView,
        baseRadius: Int,
        barrelLength: Int,
        barrelWidth: Int,
    ) {
        this.baseRadius = baseRadius
        this.barrelLength = barrelLength
        paint.strokeWidth = barrelWidth.toFloat()
        paint.setColor(Color.BLACK)
        this.view = view
        align(Math.PI / 2)
    }

    fun align(barrelAngle: Double): Unit {
        this.barrelAngle = barrelAngle
        barrelEnd.x = (barrelLength * Math.sin(barrelAngle)).toInt()
        barrelEnd.y = (-barrelLength * Math.cos(barrelAngle)).toInt() + view.getScreenHeight() / 2
    }

    fun fireCannonBall(): Unit {
        val velocityX =
            (CannonView.CANNONBALL_SPEED_PERCENT * view.getScreenWidth() * Math.sin(barrelAngle)).toInt()
        val velocityY =
            (CannonView.CANNONBALL_SPEED_PERCENT * view.getScreenWidth() * -Math.cos(barrelAngle)).toInt()
        val radius = view.getScreenHeight() * CannonView.CANNONBALL_RADIUS_PERCENT
        cannonBall = CannonBall(
            view,
            Color.BLACK,
            CannonView.CANNON_SOUND_ID,
            (-radius).toInt(),
            (view.getScreenHeight() / 2 - radius).toInt(),
            radius.toInt(),
            velocityX.toFloat(),
            velocityY.toFloat()
        )

        cannonBall!!.playSound()
    }

    fun draw(canvas: Canvas): Unit {
        canvas.drawLine(
            0f,
            (view.getScreenHeight() / 2).toFloat(),
            barrelEnd.x.toFloat(),
            barrelEnd.y.toFloat(),
            paint
        )
        canvas.drawCircle(0f, (view.getScreenHeight() / 2).toFloat(), baseRadius.toFloat(), paint)
    }

    fun getCannonBall(): CannonBall? = cannonBall
    fun removeCannonBall() {
        cannonBall = null
    }
}