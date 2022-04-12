package com.example.cannongame.sub_class

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.cannongame.R
import com.example.cannongame.base_class.Cannon
import java.util.*

class CannonView : SurfaceView, SurfaceHolder.Callback {
    private var cannonBall: CannonBall? = null
    private var blocker: Blocker? = null
    private var targets: ArrayList<Target>? = null

    private var screenWidth = 0
    private var screenHeight = 0

    private var gameOver = false
    private var timeLeft = 0.0
    private var shotsFired = 0
    private var totalElapsedTime = 0.0

    private var soundPool: SoundPool? = null
    private lateinit var soundMap: SparseIntArray

    private lateinit var textPaint: Paint
    private lateinit var backgroundPaint: Paint

    private var cannonThread: CannonThread? = null
    private var activity: Activity? = null
    private var dialogIsDisplayed = false
    private var cannon: Cannon? = null

    fun getCannonBall(): CannonBall? = cannonBall

    fun removeCannonBall(): Unit {
        cannonBall = null
    }

    fun getScreenWidth(): Int = screenWidth
    fun getScreenHeight(): Int = screenHeight
    public var _totalElapsedTime: Double
        get() = totalElapsedTime
        set(value) {
            totalElapsedTime = value
        }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        activity = context as Activity

        holder.addCallback(this)

        val attrBuilder = AudioAttributes.Builder()
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME)

        val builder = SoundPool.Builder()
        builder.setMaxStreams(1)
        builder.setAudioAttributes(attrBuilder.build())
        soundPool = builder.build()

        soundMap = SparseIntArray(3)
        soundMap.put(TARGET_SOUND_ID, soundPool!!.load(context, R.raw.target_hit, 1))
        soundMap.put(BLOCKER_SOUND_ID, soundPool!!.load(context, R.raw.blocker_hit, 1))
        soundMap.put(CANNON_SOUND_ID, soundPool!!.load(context, R.raw.cannon_fire, 1))

        textPaint = Paint()
        backgroundPaint = Paint()
        backgroundPaint.setColor(Color.WHITE)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        screenWidth = width
        screenHeight = height

        textPaint.textSize = (TEXT_SIZE_PERCENT * screenHeight).toFloat()
        textPaint.isAntiAlias = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        //TODO("Not yet implemented")
        if (!dialogIsDisplayed) {
            newGame()
            cannonThread = CannonThread(holder, this)
            cannonThread!!.setRunning(true)
            cannonThread!!.start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        //TODO("Not yet implemented")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        //TODO("Not yet implemented")
        var retry = true
        cannonThread!!.setRunning(false)
        while (retry) {
            try {
                cannonThread!!.join()
                retry = false
            } catch (e: InterruptedException) {
                Log.e(TAG, "Thread interrupted", e)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            alignAndFireCannonBall(event)
        }
        return true
    }

    fun newGame(): Unit {
        cannon = Cannon(
            this,
            (CANNON_BASE_RADIUS_PERCENT * screenHeight).toInt(),
            (CANNON_BARREL_LENGTH_PERCENT * screenWidth).toInt(),
            (CANNON_BARREL_WIDTH_PERCENT * screenHeight).toInt()
        )

        val random = Random()

        targets = ArrayList<Target>()

        var targetX = (TARGET_FIRST_X_PERCENT * screenWidth).toInt()
        val targetY = ((0.5 - TARGET_LENGTH_PERCENT / 2) * screenHeight).toInt()

        for (index in 0..(TARGET_PIECES - 1).toInt()) {
            var velocity =
                screenHeight * (random.nextDouble() * (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT) + TARGET_MIN_SPEED_PERCENT)
            val color =
                if (index % 2 == 0)
                    resources.getColor(R.color.dark, context.theme)
                else
                    resources.getColor(R.color.light, context.theme)

            velocity *= -1

            targets!!.add(
                Target(
                    this,
                    color,
                    targetX,
                    targetY,
                    (BLOCKER_WIDTH_PERCENT * screenWidth).toInt(),
                    (BLOCKER_LENGTH_PERCENT * screenHeight).toInt(),
                    velocity.toFloat(),
                    HIT_REWARD
                )
            )

            targetX += ((TARGET_WIDTH_PERCENT + TARGET_SPACING_PERCENT) * screenWidth).toInt()
        }

        blocker = Blocker(
            this,
            Color.BLACK,
            (BLOCKER_X_PERCENT * screenWidth).toInt(),
            ((0.5 - BLOCKER_LENGTH_PERCENT / 2) * screenHeight).toInt(),
            (BLOCKER_WIDTH_PERCENT * screenWidth).toInt(),
            (BLOCKER_LENGTH_PERCENT * screenHeight).toInt(),
            (BLOCKER_SPEED_PERCENT * screenHeight).toFloat(),
            MISS_PENALTY
        )

        timeLeft = 10.0
        shotsFired = 0
        totalElapsedTime = 0.0

        if (gameOver) {
            gameOver = false
            cannonThread = CannonThread(holder, this)
            cannonThread!!.setRunning(true)
            cannonThread!!.start()
        }

        hideSystemBar()
    }

    fun updatePositions(elapsedTimeMS: Double): Unit {
        val interval = elapsedTimeMS / 1000.0

        if (cannon?.getCannonBall() != null) {
            cannon!!.getCannonBall()!!.update(interval)
        }

        blocker?.update(interval)

        for (target in targets!!) {
            target.update(interval)
        }

        timeLeft -= interval

        if (timeLeft <= 0) {
            timeLeft = 0.0
            gameOver = true
            cannonThread!!.setRunning(false)
            showGameOverDialog(R.string.lose)
        }

        if (targets!!.isEmpty()) {
            gameOver = true
            cannonThread!!.setRunning(false)
            showGameOverDialog(R.string.win)
        }
    }

    fun alignAndFireCannonBall(event: MotionEvent): Unit {
        val touchPoint: Point = Point(event.getX().toInt(), event.getY().toInt())

        val centerMinusY = (screenHeight / 2 - touchPoint.y)
        val angle = Math.atan2(touchPoint.x.toDouble(), centerMinusY.toDouble())
        cannon?.align(angle)

        if (cannon?.getCannonBall() == null || !cannon!!.getCannonBall()!!.isOnScreen()) {
            cannon!!.fireCannonBall()
            shotsFired++
        }
    }

    fun showGameOverDialog(msgId: Int): Unit {
        val builder = AlertDialog.Builder(this.activity as Context)

        builder.setTitle(resources.getString(msgId))

        builder.setMessage(
            resources.getString(
                R.string.results_format,
                shotsFired,
                totalElapsedTime
            )
        )

        builder.setPositiveButton(
            R.string.reset_game,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    //TODO("Not yet implemented")
                    dialogIsDisplayed = false
                    newGame()
                }
            })

        builder.setNegativeButton(R.string.exit_game, object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                //TODO("Not yet implemented")
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        })

        builder.setCancelable(false)

        activity!!.runOnUiThread(object : Runnable {
            override fun run() {
                //TODO("Not yet implemented")
                showSystemBar()
                dialogIsDisplayed = true
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        })
    }

    fun drawGameElements(canvas: Canvas): Unit {
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), backgroundPaint)

        canvas.drawText(
            resources.getString(R.string.time_remaining_format, timeLeft),
            50f,
            100f,
            textPaint
        )

        cannon!!.draw(canvas)

        if (cannon?.getCannonBall() != null && cannon?.getCannonBall()?.isOnScreen()!!) {
            cannon?.getCannonBall()?.draw(canvas)
        }

        blocker!!.draw(canvas)

        for (target in targets!!) {
            target.draw(canvas)
        }
    }

    fun testForCollisions(): Unit {
        if (cannon?.getCannonBall() != null && cannon!!.getCannonBall()!!.isOnScreen()) {
            for (index in 0..(targets!!.size - 1)) {
                if (cannon!!.getCannonBall()!!.collidesWith(targets!!.get(index))) {
                    targets!!.get(index).playSound()

                    timeLeft += targets!!.get(index).getHitReward()

                    cannon!!.removeCannonBall()
                    targets!!.removeAt(index)

                    break
                }
            }
        } else cannon!!.removeCannonBall()

        if (cannon?.getCannonBall() != null && cannon!!.getCannonBall()!!.collidesWith(blocker!!)) {
            blocker!!.playSound()

            cannon!!.getCannonBall()!!.reverseVelocityX()

            timeLeft -= blocker!!.getMissPenalty()
        }
    }

    fun stopGame(): Unit {
        if (cannonThread != null)
            cannonThread!!.setRunning(false)
    }

    fun releaseResources(): Unit {
        soundPool!!.release()
        soundPool = null
    }

    fun playSound(soundId: Int): Unit {
        soundPool!!.play(soundMap.get(soundId), 1f, 1f, 1, 0, 1f)
    }

    fun hideSystemBar(): Unit {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE
            )
        }
    }

    fun showSystemBar(): Unit {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
    }

    companion object {
        private const val TAG = "CannonView"

        const val MISS_PENALTY = 2
        const val HIT_REWARD = 3

        const val CANNON_BASE_RADIUS_PERCENT = 3.0 / 40
        const val CANNON_BARREL_WIDTH_PERCENT = 3.0 / 40
        const val CANNON_BARREL_LENGTH_PERCENT = 1.0 / 10
        const val CANNONBALL_RADIUS_PERCENT = 3.0 / 80
        const val CANNONBALL_SPEED_PERCENT = 3.0 / 2

        const val TARGET_WIDTH_PERCENT = 1.0 / 40
        const val TARGET_LENGTH_PERCENT = 3.0 / 20
        const val TARGET_FIRST_X_PERCENT = 3.0 / 5
        const val TARGET_SPACING_PERCENT = 1.0 / 60
        const val TARGET_PIECES = 9.0
        const val TARGET_MIN_SPEED_PERCENT = 3.0 / 4
        const val TARGET_MAX_SPEED_PERCENT = 6.0 / 4

        const val BLOCKER_WIDTH_PERCENT = 1.0 / 40
        const val BLOCKER_LENGTH_PERCENT = 1.0 / 4
        const val BLOCKER_X_PERCENT = 1.0 / 2
        const val BLOCKER_SPEED_PERCENT = 1.0

        const val TEXT_SIZE_PERCENT = 1.0 / 18

        const val TARGET_SOUND_ID = 0
        const val CANNON_SOUND_ID = 1
        const val BLOCKER_SOUND_ID = 2
    }
}