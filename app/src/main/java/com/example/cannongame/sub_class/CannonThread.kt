package com.example.cannongame.sub_class

import android.graphics.Canvas
import android.view.SurfaceHolder

class CannonThread : Thread {
    private var surfaceHolder: SurfaceHolder? = null
    private var cannonView: CannonView? = null
    private var isRunning: Boolean = false

    constructor(surfaceHolder: SurfaceHolder, cannonView: CannonView) {
        this.surfaceHolder = surfaceHolder
        this.cannonView = cannonView
        name = "CannonThread"
    }

    override fun run() {
        var canvas: Canvas? = null
        var previousFrameTime: Long = System.currentTimeMillis()

        while (isRunning) {
            try {
                canvas = surfaceHolder?.lockCanvas(null)
                synchronized(surfaceHolder!!) {
                    val currentTime = System.currentTimeMillis()
                    val eslapedTimeMS = currentTime - previousFrameTime
                    cannonView!!._totalElapsedTime += eslapedTimeMS / 1000.0
                    cannonView!!.updatePositions(eslapedTimeMS.toDouble())
                    cannonView!!.testForCollisions()
                    cannonView!!.drawGameElements(canvas!!)
                    previousFrameTime = currentTime

                }
            } finally {
                if (canvas != null) {
                    surfaceHolder!!.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    fun setRunning(running: Boolean) {
        isRunning = running
    }
}