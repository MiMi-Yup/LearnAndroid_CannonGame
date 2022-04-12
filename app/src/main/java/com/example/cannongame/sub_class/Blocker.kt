package com.example.cannongame.sub_class

class Blocker(
    view: CannonView,
    color: Int,
    x: Int,
    y: Int,
    width: Int,
    length: Int,
    velocityY: Float,
    missPenalty: Int
) : GameElement(view, color, CannonView.BLOCKER_SOUND_ID, x, y, width, length, velocityY) {
    private var missPenalty: Int = missPenalty

    fun getMissPenalty(): Int = missPenalty
}