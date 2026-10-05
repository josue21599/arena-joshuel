package com.josue.arenajoshuel.model

class Tower(
    override val team: Team,
    val isKing: Boolean,
    val lane: Lane?,
    override val x: Float,
    override val y: Float,
    val maxHp: Float,
) : Targetable {
    override var hp: Float = maxHp
    override var flash: Float = 0f
    override val radius: Float get() = if (isKing) 30f else 22f
    override val isBuilding: Boolean get() = true
    var cooldown: Float = 0f
}
