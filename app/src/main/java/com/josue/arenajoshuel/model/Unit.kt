package com.josue.arenajoshuel.model

/** GROUND: ataca unidades y edificios. BUILDINGS: solo edificios. */
enum class TargetType { GROUND, BUILDINGS }

class Unit(
    override val team: Team,
    val lane: Lane,
    override var x: Float,
    override var y: Float,
    val maxHp: Float,
    val damage: Float,
    val speed: Float,
    val range: Float,
    val attackInterval: Float,
    val sight: Float = 90f,
    val targets: TargetType = TargetType.GROUND,
    val ranged: Boolean = false,
    override val radius: Float = 8f,
) : Targetable {
    override var hp: Float = maxHp
    override var flash: Float = 0f
    override val isBuilding: Boolean get() = false
    var target: Targetable? = null
    var cooldown: Float = 0f
}
