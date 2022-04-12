package com.example.cannongame.sub_class

class Target(
    view: CannonView,
    color: Int,
    x: Int,
    y: Int,
    width: Int,
    length: Int,
    velocityY: Float,
    hitReward: Int
) : GameElement(view, color, CannonView.TARGET_SOUND_ID, x, y, width, length, velocityY) {
    private var hitReward: Int = hitReward

    fun getHitReward(): Int = hitReward
}